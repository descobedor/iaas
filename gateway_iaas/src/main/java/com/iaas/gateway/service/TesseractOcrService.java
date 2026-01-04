package com.iaas.gateway.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import com.iaas.gateway.config.TesseractProperties;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

@Service
public class TesseractOcrService {

    private final ObjectProvider<Tesseract> tesseractProvider;
    private final TesseractProperties properties;

    public TesseractOcrService(ObjectProvider<Tesseract> tesseractProvider, TesseractProperties properties) {
        this.tesseractProvider = tesseractProvider;
        this.properties = properties;
    }

    public String extractMenuText(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen es obligatoria");
        }

        BufferedImage bufferedImage = preprocess(readImage(image));
        Tesseract tesseract = tesseractProvider.getObject();
        try {
            return cleanOcrText(tesseract.doOCR(bufferedImage));
        } catch (UnsatisfiedLinkError e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo cargar la librería nativa de Tesseract. Instala libtesseract en el sistema.", e);
        } catch (TesseractException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "No se pudo extraer texto de la imagen", e);
        }
    }

    public String extractMenuText(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen es obligatoria");
        }

        BufferedImage bufferedImage = preprocess(readImage(new ByteArrayInputStream(imageBytes)));
        Tesseract tesseract = tesseractProvider.getObject();
        try {
            return cleanOcrText(tesseract.doOCR(bufferedImage));
        } catch (UnsatisfiedLinkError e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo cargar la librería nativa de Tesseract. Instala libtesseract en el sistema.", e);
        } catch (TesseractException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "No se pudo extraer texto de la imagen", e);
        }
    }

    private BufferedImage readImage(MultipartFile image) {
        try {
            return readImage(image.getInputStream());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer la imagen", e);
        }
    }

    private BufferedImage readImage(InputStream inputStream) {
        try {
            byte[] bytes = inputStream.readAllBytes();
            if (bytes.length == 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen está vacía");
            }
            return decodeImage(bytes);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer la imagen", e);
        }
    }

    private BufferedImage decodeImage(byte[] bytes) {
        try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            if (imageInputStream == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo abrir la imagen");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
            if (!readers.hasNext()) {
                BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(bytes));
                if (bufferedImage != null) {
                    return bufferedImage;
                }
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de imagen no soportado o imagen corrupta");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(imageInputStream, true, true);
                BufferedImage bufferedImage = reader.read(0);
                if (bufferedImage == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de imagen no soportado o imagen corrupta");
                }
                return bufferedImage;
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer la imagen", e);
        }
    }

    private BufferedImage preprocess(BufferedImage input) {
        if (input == null) {
            return null;
        }
        BufferedImage grayscale = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D grayscaleGraphics = grayscale.createGraphics();
        grayscaleGraphics.drawImage(input, 0, 0, null);
        grayscaleGraphics.dispose();

        double scale = properties.getScale() != null ? properties.getScale() : 1.0;
        if (scale <= 1.0) {
            return grayscale;
        }
        int scaledWidth = Math.max(1, (int) Math.round(grayscale.getWidth() * scale));
        int scaledHeight = Math.max(1, (int) Math.round(grayscale.getHeight() * scale));
        BufferedImage scaled = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D scaledGraphics = scaled.createGraphics();
        scaledGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        scaledGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        scaledGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        scaledGraphics.drawImage(grayscale, 0, 0, scaledWidth, scaledHeight, null);
        scaledGraphics.dispose();
        return scaled;
    }

    private String cleanOcrText(String text) {
        if (text == null) {
            return null;
        }
        int minLength = properties.getMinLineLength() != null ? properties.getMinLineLength() : 0;
        double minAlnumRatio = properties.getMinAlnumRatio() != null ? properties.getMinAlnumRatio() : 0.0;
        StringBuilder builder = new StringBuilder();
        for (String line : text.split("\\R")) {
            String normalized = line.replaceAll("\\s+", " ").trim();
            if (normalized.length() < minLength) {
                continue;
            }
            int alnumCount = 0;
            int totalCount = 0;
            for (char ch : normalized.toCharArray()) {
                if (!Character.isWhitespace(ch)) {
                    totalCount++;
                }
                if (Character.isLetterOrDigit(ch)) {
                    alnumCount++;
                }
            }
            if (totalCount > 0 && ((double) alnumCount / totalCount) < minAlnumRatio) {
                continue;
            }
            builder.append(normalized).append('\n');
        }
        return builder.toString().trim();
    }
}

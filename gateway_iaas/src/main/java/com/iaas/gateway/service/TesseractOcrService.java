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

    private static final java.util.regex.Pattern PRICE_PATTERN =
            java.util.regex.Pattern.compile("(\\d+[\\.,]\\d{2})");

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
        return runOcr(bufferedImage);
    }

    public String extractMenuText(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen es obligatoria");
        }

        BufferedImage bufferedImage = preprocess(readImage(new ByteArrayInputStream(imageBytes)));
        return runOcr(bufferedImage);
    }

    public com.iaas.gateway.api.MenuStructuredResponse extractMenuStructured(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen es obligatoria");
        }
        BufferedImage bufferedImage = preprocess(readImage(image));
        String text = runOcr(bufferedImage);
        return parseMenu(text);
    }

    public com.iaas.gateway.api.MenuStructuredResponse extractMenuStructured(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen es obligatoria");
        }
        BufferedImage bufferedImage = preprocess(readImage(new ByteArrayInputStream(imageBytes)));
        String text = runOcr(bufferedImage);
        return parseMenu(text);
    }

    private String runOcr(BufferedImage bufferedImage) {
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

    private com.iaas.gateway.api.MenuStructuredResponse parseMenu(String text) {
        java.util.List<com.iaas.gateway.api.MenuSection> sections = new java.util.ArrayList<>();
        com.iaas.gateway.api.MenuSection current = null;
        if (text == null || text.isBlank()) {
            return new com.iaas.gateway.api.MenuStructuredResponse(sections, text == null ? "" : text);
        }
        for (String line : text.split("\\R")) {
            String normalized = line.replaceAll("\\s+", " ").trim();
            if (normalized.isBlank()) {
                continue;
            }
            java.util.regex.Matcher matcher = PRICE_PATTERN.matcher(normalized);
            if (!matcher.find()) {
                String categoryName = cleanMenuLabel(normalized);
                if (isCategory(categoryName)) {
                    current = new com.iaas.gateway.api.MenuSection(categoryName, new java.util.ArrayList<>());
                    sections.add(current);
                }
                continue;
            }
            if (current == null) {
                current = new com.iaas.gateway.api.MenuSection("SIN CATEGORÍA", new java.util.ArrayList<>());
                sections.add(current);
            }
            String price = matcher.group(1).replace(',', '.');
            String name = normalized.substring(0, matcher.start()).replaceAll("[-–—:]+$", "").trim();
            if (name.isBlank()) {
                name = normalized;
            }
            current.items().add(new com.iaas.gateway.api.MenuItem(cleanMenuLabel(name), price));
        }
        return new com.iaas.gateway.api.MenuStructuredResponse(sections, text);
    }

    private boolean isCategory(String line) {
        if (line == null) {
            return false;
        }
        String normalized = line.trim();
        if (normalized.length() < 3) {
            return false;
        }
        if (PRICE_PATTERN.matcher(normalized).find()) {
            return false;
        }
        boolean hasLetter = false;
        int letterCount = 0;
        int alnumCount = 0;
        int totalCount = 0;
        for (char ch : normalized.toCharArray()) {
            if (Character.isLetter(ch)) {
                hasLetter = true;
                letterCount++;
            }
            if (Character.isLetter(ch) && Character.isLowerCase(ch)) {
                return false;
            }
            if (!Character.isWhitespace(ch)) {
                totalCount++;
            }
            if (Character.isLetterOrDigit(ch)) {
                alnumCount++;
            }
        }
        if (!hasLetter || letterCount < 3) {
            return false;
        }
        return totalCount == 0 || ((double) alnumCount / totalCount) >= 0.6;
    }

    private String cleanMenuLabel(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.replaceAll("^\\P{L}+", "")
                .replaceAll("^(?:[\\p{Alpha}]{1,3}\\s*){1,3}[-–—]\\s*", "")
                .replaceAll("^(?:[\\p{Alpha}]{1,3}[\\.)]?\\s*){1,3}[-–—]\\s*", "")
                .replaceAll("^[\\p{Alpha}]{1,3}\\)\\s*[\\p{Alpha}]?\\s*[-–—]\\s*", "")
                .replaceAll("^\\p{Alpha}\\.?\\s*[-–—]\\s*", "")
                .replaceAll("^o\\s+", "")
                .replaceAll("^[^\\p{Alnum}]+", "")
                .replaceAll("\\s+", " ")
                .trim();
        return cleaned.isBlank() ? value.trim() : cleaned;
    }
}

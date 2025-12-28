package com.iaas.gateway.service;

import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

@Service
public class TesseractOcrService {

    private final ObjectProvider<Tesseract> tesseractProvider;

    public TesseractOcrService(ObjectProvider<Tesseract> tesseractProvider) {
        this.tesseractProvider = tesseractProvider;
    }

    public String extractMenuText(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen es obligatoria");
        }

        BufferedImage bufferedImage = readImage(image);
        Tesseract tesseract = tesseractProvider.getObject();
        try {
            return tesseract.doOCR(bufferedImage);
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

        BufferedImage bufferedImage = readImage(new ByteArrayInputStream(imageBytes));
        Tesseract tesseract = tesseractProvider.getObject();
        try {
            return tesseract.doOCR(bufferedImage);
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
}

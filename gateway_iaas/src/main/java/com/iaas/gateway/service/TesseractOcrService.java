package com.iaas.gateway.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class TesseractOcrService {

    private final Tesseract tesseract;

    public TesseractOcrService(Tesseract tesseract) {
        this.tesseract = tesseract;
    }

    public String extractMenuText(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen es obligatoria");
        }

        BufferedImage bufferedImage = readImage(image);
        try {
            return tesseract.doOCR(bufferedImage);
        } catch (TesseractException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "No se pudo extraer texto de la imagen", e);
        }
    }

    public String extractMenuText(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen es obligatoria");
        }

        BufferedImage bufferedImage = readImage(new ByteArrayInputStream(imageBytes));
        try {
            return tesseract.doOCR(bufferedImage);
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
            BufferedImage bufferedImage = ImageIO.read(inputStream);
            if (bufferedImage == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de imagen no soportado");
            }
            return bufferedImage;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer la imagen", e);
        }
    }
}

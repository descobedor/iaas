package com.iaas.gateway.api;

import com.iaas.gateway.service.TesseractOcrService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;
import java.util.Iterator;
import java.util.Map;

@RestController
@RequestMapping("/ocr")
public class OcrController {

    private final TesseractOcrService ocrService;

    public OcrController(TesseractOcrService ocrService) {
        this.ocrService = ocrService;
    }

    @PostMapping(value = "/menu", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public OcrResponse extractMenu(@RequestPart(value = "image", required = false) MultipartFile image,
                                   MultipartHttpServletRequest request) {
        MultipartFile resolvedImage = image != null ? image : resolveAnyFile(request);
        if (resolvedImage == null || resolvedImage.isEmpty()) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,
                    "La parte 'image' es obligatoria (ej: -F image=@archivo.jpg)");
        }
        return new OcrResponse(ocrService.extractMenuText(resolvedImage));
    }

    @PostMapping(value = "/menu", consumes = MediaType.APPLICATION_JSON_VALUE)
    public OcrResponse extractMenuBase64(@RequestBody OcrBase64Request request) {
        byte[] imageBytes = decodeBase64(request);
        return new OcrResponse(ocrService.extractMenuText(imageBytes));
    }

    @PostMapping(value = "/menu/structured", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MenuStructuredResponse extractMenuStructured(@RequestPart(value = "image", required = false) MultipartFile image,
                                                        MultipartHttpServletRequest request) {
        MultipartFile resolvedImage = image != null ? image : resolveAnyFile(request);
        if (resolvedImage == null || resolvedImage.isEmpty()) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,
                    "La parte 'image' es obligatoria (ej: -F image=@archivo.jpg)");
        }
        return ocrService.extractMenuStructured(resolvedImage);
    }

    @PostMapping(value = "/menu/structured", consumes = MediaType.APPLICATION_JSON_VALUE)
    public MenuStructuredResponse extractMenuStructuredBase64(@RequestBody OcrBase64Request request) {
        byte[] imageBytes = decodeBase64(request);
        return ocrService.extractMenuStructured(imageBytes);
    }

    private byte[] decodeBase64(OcrBase64Request request) {
        if (request == null || request.imageBase64() == null || request.imageBase64().isBlank()) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "imageBase64 es obligatorio");
        }
        try {
            return Base64.getDecoder().decode(request.imageBase64());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "imageBase64 no es válido", e);
        }
    }

    private MultipartFile resolveAnyFile(MultipartHttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Map<String, MultipartFile> files = request.getFileMap();
        if (files == null || files.isEmpty()) {
            return null;
        }
        Iterator<MultipartFile> iterator = files.values().iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }
}

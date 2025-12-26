package com.iaas.gateway.api;

import com.iaas.gateway.service.TesseractOcrService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/ocr")
public class OcrController {

    private final TesseractOcrService ocrService;

    public OcrController(TesseractOcrService ocrService) {
        this.ocrService = ocrService;
    }

    @PostMapping(value = "/menu", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public OcrResponse extractMenu(@RequestPart("image") MultipartFile image) {
        return new OcrResponse(ocrService.extractMenuText(image));
    }
}

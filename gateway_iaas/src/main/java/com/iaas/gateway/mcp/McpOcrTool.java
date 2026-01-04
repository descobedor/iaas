package com.iaas.gateway.mcp;

import com.iaas.gateway.service.TesseractOcrService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;

@Component
public class McpOcrTool {

    private final TesseractOcrService ocrService;

    public McpOcrTool(TesseractOcrService ocrService) {
        this.ocrService = ocrService;
    }

    @Tool(name = "extract_menu_text", description = "Extrae el texto de un menú desde una imagen en base64.")
    public String extractMenuText(@ToolParam("imageBase64") String imageBase64) {
        if (imageBase64 == null || imageBase64.isBlank()) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "imageBase64 es obligatorio");
        }
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(imageBase64);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "imageBase64 no es válido", e);
        }
        return ocrService.extractMenuText(bytes);
    }
}

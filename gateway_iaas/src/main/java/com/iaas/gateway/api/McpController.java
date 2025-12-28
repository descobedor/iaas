package com.iaas.gateway.api;

import com.iaas.gateway.service.TesseractOcrService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mcp")
public class McpController {

    private static final String JSON_RPC_VERSION = "2.0";
    private static final String TOOL_EXTRACT_MENU = "extract_menu_text";

    private final TesseractOcrService ocrService;

    public McpController(TesseractOcrService ocrService) {
        this.ocrService = ocrService;
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, "application/*+json"},
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonRpcResponse> handle(@RequestBody JsonRpcRequest request) {
        if (request == null || request.method() == null) {
            return ResponseEntity.badRequest().body(error(request, -32600, "Invalid Request"));
        }
        return switch (request.method()) {
            case "tools/list" -> ResponseEntity.ok(listTools(request));
            case "tools/call" -> handleToolCall(request);
            default -> ResponseEntity.badRequest().body(error(request, -32601, "Method not found"));
        };
    }

    private JsonRpcResponse listTools(JsonRpcRequest request) {
        Map<String, Object> tool = new LinkedHashMap<>();
        tool.put("name", TOOL_EXTRACT_MENU);
        tool.put("description", "Extrae el texto de un menú desde una imagen en base64.");
        tool.put("inputSchema", Map.of(
                "type", "object",
                "properties", Map.of("imageBase64", Map.of("type", "string")),
                "required", List.of("imageBase64")
        ));
        Map<String, Object> result = Map.of("tools", List.of(tool));
        return new JsonRpcResponse(JSON_RPC_VERSION, request.id(), result, null);
    }

    private ResponseEntity<JsonRpcResponse> handleToolCall(JsonRpcRequest request) {
        Map<String, Object> params = request.params();
        if (params == null) {
            return ResponseEntity.badRequest().body(error(request, -32602, "Missing params"));
        }
        Object name = params.get("name");
        Object arguments = params.get("arguments");
        if (!TOOL_EXTRACT_MENU.equals(name) || !(arguments instanceof Map<?, ?> args)) {
            return ResponseEntity.badRequest().body(error(request, -32602, "Invalid tool call"));
        }
        Object imageBase64 = args.get("imageBase64");
        if (!(imageBase64 instanceof String imageString) || imageString.isBlank()) {
            return ResponseEntity.badRequest().body(error(request, -32602, "imageBase64 es obligatorio"));
        }
        byte[] imageBytes;
        try {
            imageBytes = Base64.getDecoder().decode(imageString);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(error(request, -32602, "imageBase64 no es válido"));
        }
        String text = ocrService.extractMenuText(imageBytes);
        Map<String, Object> result = Map.of(
                "content", List.of(Map.of("type", "text", "text", text))
        );
        return ResponseEntity.ok(new JsonRpcResponse(JSON_RPC_VERSION, request.id(), result, null));
    }

    private JsonRpcResponse error(JsonRpcRequest request, int code, String message) {
        Object id = request == null ? null : request.id();
        return new JsonRpcResponse(JSON_RPC_VERSION, id, null, new JsonRpcError(code, message));
    }

    record JsonRpcRequest(String jsonrpc, Object id, String method, Map<String, Object> params) {
    }

    record JsonRpcResponse(String jsonrpc, Object id, Map<String, Object> result, JsonRpcError error) {
    }

    record JsonRpcError(int code, String message) {
    }
}

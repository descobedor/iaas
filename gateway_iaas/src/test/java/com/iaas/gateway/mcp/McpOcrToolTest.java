package com.iaas.gateway.mcp;

import com.iaas.gateway.service.TesseractOcrService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class McpOcrToolTest {

    @Test
    void extractMenuTextDecodesBase64AndCallsService() {
        TesseractOcrService service = mock(TesseractOcrService.class);
        when(service.extractMenuText(any(byte[].class))).thenReturn("menu text");
        McpOcrTool tool = new McpOcrTool(service);

        String base64 = Base64.getEncoder().encodeToString("image".getBytes());

        String result = tool.extractMenuText(base64);

        assertThat(result).isEqualTo("menu text");
    }

    @Test
    void extractMenuTextRejectsInvalidBase64() {
        TesseractOcrService service = mock(TesseractOcrService.class);
        McpOcrTool tool = new McpOcrTool(service);

        assertThatThrownBy(() -> tool.extractMenuText("not-base64"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException response = (ResponseStatusException) ex;
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }
}

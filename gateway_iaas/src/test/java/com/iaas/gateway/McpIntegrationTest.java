package com.iaas.gateway;

import com.iaas.gateway.service.TesseractOcrService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class McpIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TesseractOcrService ocrService;

    @Test
    void mcpToolCallConsumesOcrService() throws Exception {
        when(ocrService.extractMenuText(any(byte[].class))).thenReturn("menu text");
        String base64 = Base64.getEncoder().encodeToString(createTestPng());

        String payload = "{" +
                "\"jsonrpc\":\"2.0\"," +
                "\"id\":\"1\"," +
                "\"method\":\"tools/call\"," +
                "\"params\":{\"name\":\"extract_menu_text\",\"arguments\":{\"imageBase64\":\"" + base64 + "\"}}" +
                "}";

        mockMvc.perform(post("/mcp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.content[0].text").value("menu text"));
    }

    private byte[] createTestPng() throws Exception {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return outputStream.toByteArray();
    }
}

package com.iaas.gateway;

import com.iaas.gateway.service.TesseractOcrService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OcrControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TesseractOcrService ocrService;

    @Test
    void multipartOcrFlowReturnsText() throws Exception {
        when(ocrService.extractMenuText(any(org.springframework.web.multipart.MultipartFile.class)))
                .thenReturn("menu text");

        MockMultipartFile file = new MockMultipartFile("image", "menu.png", "image/png", createTestPng());

        mockMvc.perform(multipart("/ocr/menu")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("menu text"));
    }

    @Test
    void multipartStructuredOcrFlowReturnsSections() throws Exception {
        when(ocrService.extractMenuStructured(any(org.springframework.web.multipart.MultipartFile.class)))
                .thenReturn(new com.iaas.gateway.api.MenuStructuredResponse(java.util.List.of(), "raw"));

        MockMultipartFile file = new MockMultipartFile("image", "menu.png", "image/png", createTestPng());

        mockMvc.perform(multipart("/ocr/menu/structured")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rawText").value("raw"));
    }

    private byte[] createTestPng() throws Exception {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return outputStream.toByteArray();
    }
}

package com.iaas.gateway.api;

import com.iaas.gateway.service.TesseractOcrService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OcrController.class)
class OcrControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TesseractOcrService ocrService;

    @Test
    void extractMenuAcceptsMultipartImage() throws Exception {
        when(ocrService.extractMenuText(org.mockito.ArgumentMatchers.any(org.springframework.web.multipart.MultipartFile.class)))
                .thenReturn("menu text");

        MockMultipartFile file = new MockMultipartFile("image", "menu.png", "image/png", createTestPng());

        mockMvc.perform(multipart("/ocr/menu")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("menu text"));
    }

    @Test
    void extractMenuReturnsBadRequestWhenMissingImage() throws Exception {
        mockMvc.perform(multipart("/ocr/menu")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void extractMenuAcceptsBase64Json() throws Exception {
        when(ocrService.extractMenuText(org.mockito.ArgumentMatchers.any(byte[].class)))
                .thenReturn("menu text");

        String base64 = Base64.getEncoder().encodeToString(createTestPng());

        mockMvc.perform(post("/ocr/menu")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"imageBase64\":\"" + base64 + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("menu text"));
    }

    @Test
    void extractMenuStructuredAcceptsBase64Json() throws Exception {
        when(ocrService.extractMenuStructured(org.mockito.ArgumentMatchers.any(byte[].class)))
                .thenReturn(new MenuStructuredResponse(java.util.List.of(), "raw"));

        String base64 = Base64.getEncoder().encodeToString(createTestPng());

        mockMvc.perform(post("/ocr/menu/structured")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"imageBase64\":\"" + base64 + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rawText").value("raw"));
    }

    @Test
    void extractMenuRejectsInvalidBase64() throws Exception {
        mockMvc.perform(post("/ocr/menu")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"imageBase64\":\"not-base64\"}"))
                .andExpect(status().isBadRequest());
    }

    private byte[] createTestPng() throws Exception {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return outputStream.toByteArray();
    }
}

package com.iaas.gateway.service;

import net.sourceforge.tess4j.Tesseract;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TesseractOcrServiceTest {

    @Test
    void extractMenuTextReturnsOcrResult() throws Exception {
        Tesseract tesseract = mock(Tesseract.class);
        when(tesseract.doOCR(any(BufferedImage.class))).thenReturn("menu text");
        TesseractOcrService service = new TesseractOcrService(tesseract);

        byte[] imageBytes = createTestPng();

        String result = service.extractMenuText(imageBytes);

        assertThat(result).isEqualTo("menu text");
    }

    @Test
    void extractMenuTextThrowsServiceUnavailableWhenNativeLibraryMissing() throws Exception {
        Tesseract tesseract = mock(Tesseract.class);
        when(tesseract.doOCR(any(BufferedImage.class))).thenThrow(new UnsatisfiedLinkError("missing"));
        TesseractOcrService service = new TesseractOcrService(tesseract);

        MockMultipartFile file = new MockMultipartFile("image", "menu.png", "image/png", createTestPng());

        assertThatThrownBy(() -> service.extractMenuText(file))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException response = (ResponseStatusException) ex;
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                });
    }

    private byte[] createTestPng() throws IOException {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return outputStream.toByteArray();
    }
}

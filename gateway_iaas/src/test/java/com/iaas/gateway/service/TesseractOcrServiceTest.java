package com.iaas.gateway.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;
import net.sourceforge.tess4j.Tesseract;
import com.iaas.gateway.config.TesseractProperties;

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
        ObjectProvider<Tesseract> provider = mock(ObjectProvider.class);
        when(provider.getObject()).thenReturn(tesseract);
        TesseractOcrService service = new TesseractOcrService(provider, new TesseractProperties());

        byte[] imageBytes = createTestPng();

        String result = service.extractMenuText(imageBytes);

        assertThat(result).isEqualTo("menu text");
    }

    @Test
    void extractMenuTextThrowsServiceUnavailableWhenNativeLibraryMissing() throws Exception {
        Tesseract tesseract = mock(Tesseract.class);
        when(tesseract.doOCR(any(BufferedImage.class))).thenThrow(new UnsatisfiedLinkError("missing"));
        ObjectProvider<Tesseract> provider = mock(ObjectProvider.class);
        when(provider.getObject()).thenReturn(tesseract);
        TesseractOcrService service = new TesseractOcrService(provider, new TesseractProperties());

        MockMultipartFile file = new MockMultipartFile("image", "menu.png", "image/png", createTestPng());

        assertThatThrownBy(() -> service.extractMenuText(file))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException response = (ResponseStatusException) ex;
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                });
    }

    @Test
    void extractMenuTextCleansNoisyLines() throws Exception {
        Tesseract tesseract = mock(Tesseract.class);
        when(tesseract.doOCR(any(BufferedImage.class))).thenReturn("A\nCOCHINITA PIBIL 13,00\n?\n");
        ObjectProvider<Tesseract> provider = mock(ObjectProvider.class);
        when(provider.getObject()).thenReturn(tesseract);
        TesseractProperties properties = new TesseractProperties();
        properties.setMinLineLength(3);
        properties.setMinAlnumRatio(0.5);
        TesseractOcrService service = new TesseractOcrService(provider, properties);

        String result = service.extractMenuText(createTestPng());

        assertThat(result).isEqualTo("COCHINITA PIBIL 13,00");
    }

    private byte[] createTestPng() throws IOException {
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return outputStream.toByteArray();
    }
}

package com.iaas.gateway.config;

import net.sourceforge.tess4j.Tesseract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;

@Configuration
public class TesseractConfig {

    @PostConstruct
    public void registerImageIoPlugins() {
        ImageIO.scanForPlugins();
        ImageIO.setUseCache(false);
    }

    @Bean
    public Tesseract tesseract(TesseractProperties properties) {
        Tesseract tesseract = new Tesseract();
        if (properties.getDataPath() != null && !properties.getDataPath().isBlank()) {
            tesseract.setDatapath(properties.getDataPath());
        }
        if (properties.getLanguage() != null && !properties.getLanguage().isBlank()) {
            tesseract.setLanguage(properties.getLanguage());
        }
        return tesseract;
    }
}

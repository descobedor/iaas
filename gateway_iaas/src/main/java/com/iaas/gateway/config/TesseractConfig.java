package com.iaas.gateway.config;

import net.sourceforge.tess4j.Tesseract;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

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
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public Tesseract tesseract(TesseractProperties properties) {
        Tesseract tesseract = new Tesseract();
        if (properties.getDataPath() != null && !properties.getDataPath().isBlank()) {
            tesseract.setDatapath(properties.getDataPath());
        }
        if (properties.getLanguage() != null && !properties.getLanguage().isBlank()) {
            tesseract.setLanguage(properties.getLanguage());
        }
        if (properties.getOcrEngineMode() != null) {
            tesseract.setOcrEngineMode(properties.getOcrEngineMode());
        }
        if (properties.getPageSegMode() != null) {
            tesseract.setPageSegMode(properties.getPageSegMode());
        }
        if (properties.getUserDefinedDpi() != null) {
            tesseract.setTessVariable("user_defined_dpi", properties.getUserDefinedDpi().toString());
        }
        return tesseract;
    }
}

package com.iaas.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.tesseract")
public class TesseractProperties {

    private String dataPath;
    private String language = "spa";
    private Integer ocrEngineMode = 1;
    private Integer pageSegMode = 6;
    private Integer userDefinedDpi = 300;
    private Double scale = 2.0;
    private Integer minLineLength = 3;
    private Double minAlnumRatio = 0.4;

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Integer getOcrEngineMode() {
        return ocrEngineMode;
    }

    public void setOcrEngineMode(Integer ocrEngineMode) {
        this.ocrEngineMode = ocrEngineMode;
    }

    public Integer getPageSegMode() {
        return pageSegMode;
    }

    public void setPageSegMode(Integer pageSegMode) {
        this.pageSegMode = pageSegMode;
    }

    public Integer getUserDefinedDpi() {
        return userDefinedDpi;
    }

    public void setUserDefinedDpi(Integer userDefinedDpi) {
        this.userDefinedDpi = userDefinedDpi;
    }

    public Double getScale() {
        return scale;
    }

    public void setScale(Double scale) {
        this.scale = scale;
    }

    public Integer getMinLineLength() {
        return minLineLength;
    }

    public void setMinLineLength(Integer minLineLength) {
        this.minLineLength = minLineLength;
    }

    public Double getMinAlnumRatio() {
        return minAlnumRatio;
    }

    public void setMinAlnumRatio(Double minAlnumRatio) {
        this.minAlnumRatio = minAlnumRatio;
    }
}

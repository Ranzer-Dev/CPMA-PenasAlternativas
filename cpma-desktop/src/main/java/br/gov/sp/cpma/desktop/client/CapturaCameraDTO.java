package br.gov.sp.cpma.desktop.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CapturaCameraDTO {

    private boolean success;
    private String base64;
    private String rawBase64;
    private boolean faceDetected;
    private int count;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getBase64() {
        return base64;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }

    public String getRawBase64() {
        return rawBase64;
    }

    public void setRawBase64(String rawBase64) {
        this.rawBase64 = rawBase64;
    }

    public boolean isFaceDetected() {
        return faceDetected;
    }

    public void setFaceDetected(boolean faceDetected) {
        this.faceDetected = faceDetected;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}

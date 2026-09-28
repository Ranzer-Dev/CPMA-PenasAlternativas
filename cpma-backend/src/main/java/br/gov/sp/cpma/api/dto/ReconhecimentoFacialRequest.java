package br.gov.sp.cpma.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public class ReconhecimentoFacialRequest {

    @NotBlank(message = "A foto capturada e obrigatoria.")
    @JsonAlias({"fotoBase64", "fotoCapturadaBase64"})
    private String fotoCapturadaBase64;

    private Double threshold;

    public String getFotoCapturadaBase64() {
        return fotoCapturadaBase64;
    }

    public void setFotoCapturadaBase64(String fotoCapturadaBase64) {
        this.fotoCapturadaBase64 = fotoCapturadaBase64;
    }

    public Double getThreshold() {
        return threshold != null && threshold > 0 ? threshold : 0.70;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }
}

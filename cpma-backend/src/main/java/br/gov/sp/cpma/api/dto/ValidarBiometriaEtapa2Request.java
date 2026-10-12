package br.gov.sp.cpma.api.dto;

import jakarta.validation.constraints.NotBlank;

public class ValidarBiometriaEtapa2Request {

    @NotBlank(message = "O codigo de acesso e obrigatorio.")
    private String codigo;

    @NotBlank(message = "A foto capturada e obrigatoria para a avaliacao biometrica.")
    private String fotoCapturadaBase64;

    private String terminalId;

    private Double threshold = 0.65;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getFotoCapturadaBase64() {
        return fotoCapturadaBase64;
    }

    public void setFotoCapturadaBase64(String fotoCapturadaBase64) {
        this.fotoCapturadaBase64 = fotoCapturadaBase64;
    }

    public String getTerminalId() {
        return terminalId != null ? terminalId : "TOTEM_DEFAULT";
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public Double getThreshold() {
        return threshold != null ? threshold : 0.65;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }
}

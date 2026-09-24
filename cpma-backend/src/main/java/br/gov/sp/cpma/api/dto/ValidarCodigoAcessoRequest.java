package br.gov.sp.cpma.api.dto;

import jakarta.validation.constraints.NotBlank;

public class ValidarCodigoAcessoRequest {

    @NotBlank(message = "O codigo de acesso e obrigatorio.")
    private String codigo;

    private String terminalId;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getTerminalId() {
        return terminalId != null ? terminalId : "TOTEM_DEFAULT";
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }
}

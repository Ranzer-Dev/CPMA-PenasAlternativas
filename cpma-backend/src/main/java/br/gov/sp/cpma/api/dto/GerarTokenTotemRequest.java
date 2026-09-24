package br.gov.sp.cpma.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class GerarTokenTotemRequest {

    @NotBlank(message = "O identificador do terminal e obrigatorio.")
    private String terminalId;

    @NotNull(message = "O ID do administrador responsavel e obrigatorio.")
    private Long adminId;

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }
}

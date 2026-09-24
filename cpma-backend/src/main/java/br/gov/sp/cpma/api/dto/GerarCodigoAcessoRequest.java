package br.gov.sp.cpma.api.dto;

import jakarta.validation.constraints.NotNull;

public class GerarCodigoAcessoRequest {

    @NotNull(message = "O ID do apenado e obrigatorio.")
    private Long usuarioId;

    @NotNull(message = "O ID do administrador responsavel e obrigatorio.")
    private Long adminId;

    private Integer minutosValidade;

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public Integer getMinutosValidade() {
        return minutosValidade != null && minutosValidade > 0 ? minutosValidade : 60;
    }

    public void setMinutosValidade(Integer minutosValidade) {
        this.minutosValidade = minutosValidade;
    }
}

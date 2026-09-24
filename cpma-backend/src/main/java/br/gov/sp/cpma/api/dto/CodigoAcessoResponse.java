package br.gov.sp.cpma.api.dto;

import java.time.LocalDateTime;

public class CodigoAcessoResponse {

    private String codigo;
    private Long usuarioId;
    private String usuarioNome;
    private LocalDateTime dataGeracao;
    private LocalDateTime dataExpiracao;
    private String status;

    public CodigoAcessoResponse() {}

    public CodigoAcessoResponse(String codigo, Long usuarioId, String usuarioNome, LocalDateTime dataGeracao, LocalDateTime dataExpiracao, String status) {
        this.codigo = codigo;
        this.usuarioId = usuarioId;
        this.usuarioNome = usuarioNome;
        this.dataGeracao = dataGeracao;
        this.dataExpiracao = dataExpiracao;
        this.status = status;
    }

    public String getCodigo() {
        return codigo;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getUsuarioNome() {
        return usuarioNome;
    }

    public LocalDateTime getDataGeracao() {
        return dataGeracao;
    }

    public LocalDateTime getDataExpiracao() {
        return dataExpiracao;
    }

    public String getStatus() {
        return status;
    }
}

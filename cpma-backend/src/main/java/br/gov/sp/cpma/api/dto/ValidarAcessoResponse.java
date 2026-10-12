package br.gov.sp.cpma.api.dto;

import java.time.LocalDateTime;

public class ValidarAcessoResponse {

    private boolean sucesso;
    private String mensagem;
    private Long usuarioId;
    private String usuarioNome;
    private String usuarioCodigo;
    private String tokenSessao;
    private LocalDateTime dataHora;

    public ValidarAcessoResponse() {
        this.dataHora = LocalDateTime.now();
    }

    public ValidarAcessoResponse(boolean sucesso, String mensagem, Long usuarioId, String usuarioNome, String usuarioCodigo) {
        this();
        this.sucesso = sucesso;
        this.mensagem = mensagem;
        this.usuarioId = usuarioId;
        this.usuarioNome = usuarioNome;
        this.usuarioCodigo = usuarioCodigo;
    }

    public ValidarAcessoResponse(boolean sucesso, String mensagem, Long usuarioId, String usuarioNome, String usuarioCodigo, String tokenSessao) {
        this(sucesso, mensagem, usuarioId, usuarioNome, usuarioCodigo);
        this.tokenSessao = tokenSessao;
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public void setSucesso(boolean sucesso) {
        this.sucesso = sucesso;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNome() {
        return usuarioNome;
    }

    public void setUsuarioNome(String usuarioNome) {
        this.usuarioNome = usuarioNome;
    }

    public String getUsuarioCodigo() {
        return usuarioCodigo;
    }

    public void setUsuarioCodigo(String usuarioCodigo) {
        this.usuarioCodigo = usuarioCodigo;
    }

    public String getTokenSessao() {
        return tokenSessao;
    }

    public void setTokenSessao(String tokenSessao) {
        this.tokenSessao = tokenSessao;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
}

package br.gov.sp.cpma.desktop.client;

import java.time.LocalDateTime;

public class ValidarCodigoEtapa1DTO {

    private boolean sucesso;
    private String mensagem;
    private Long usuarioId;
    private String usuarioNome;
    private String usuarioCodigo;
    private String codigo;
    private boolean etapaBiometriaObrigatoria;
    private LocalDateTime dataHora;

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

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public boolean isEtapaBiometriaObrigatoria() {
        return etapaBiometriaObrigatoria;
    }

    public void setEtapaBiometriaObrigatoria(boolean etapaBiometriaObrigatoria) {
        this.etapaBiometriaObrigatoria = etapaBiometriaObrigatoria;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
}

package br.gov.sp.cpma.api.dto;

public class ReconhecimentoFacialResponse {

    private boolean sucesso;
    private String mensagem;
    private Long usuarioId;
    private String usuarioNome;
    private String usuarioCodigo;
    private Double confidence;

    public ReconhecimentoFacialResponse() {}

    public ReconhecimentoFacialResponse(boolean sucesso, String mensagem, Long usuarioId, String usuarioNome, String usuarioCodigo, Double confidence) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
        this.usuarioId = usuarioId;
        this.usuarioNome = usuarioNome;
        this.usuarioCodigo = usuarioCodigo;
        this.confidence = confidence;
    }

    public static ReconhecimentoFacialResponse falha(String mensagem) {
        return new ReconhecimentoFacialResponse(false, mensagem, null, null, null, 0.0);
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

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
}

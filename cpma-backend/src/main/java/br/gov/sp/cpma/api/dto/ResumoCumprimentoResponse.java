package br.gov.sp.cpma.api.dto;

public class ResumoCumprimentoResponse {

    private Long penaId;
    private Long usuarioId;
    private String usuarioNome;
    private Integer horasTotais;
    private Double horasCumpridas;
    private Double horasRestantes;
    private Double percentualConcluido;
    private String status;

    public ResumoCumprimentoResponse() {}

    public ResumoCumprimentoResponse(Long penaId, Long usuarioId, String usuarioNome, Integer horasTotais, Double horasCumpridas) {
        this.penaId = penaId;
        this.usuarioId = usuarioId;
        this.usuarioNome = usuarioNome;
        this.horasTotais = horasTotais != null ? horasTotais : 0;
        this.horasCumpridas = horasCumpridas != null ? horasCumpridas : 0.0;
        this.horasRestantes = Math.max(0.0, this.horasTotais - this.horasCumpridas);
        this.percentualConcluido = this.horasTotais > 0 ? Math.min(100.0, (this.horasCumpridas / this.horasTotais) * 100.0) : 0.0;
        this.status = this.horasRestantes <= 0.0 ? "CONCLUIDA" : "EM_CUMPRIMENTO";
    }

    public Long getPenaId() { return penaId; }
    public void setPenaId(Long penaId) { this.penaId = penaId; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getUsuarioNome() { return usuarioNome; }
    public void setUsuarioNome(String usuarioNome) { this.usuarioNome = usuarioNome; }
    public Integer getHorasTotais() { return horasTotais; }
    public void setHorasTotais(Integer horasTotais) { this.horasTotais = horasTotais; }
    public Double getHorasCumpridas() { return horasCumpridas; }
    public void setHorasCumpridas(Double horasCumpridas) { this.horasCumpridas = horasCumpridas; }
    public Double getHorasRestantes() { return horasRestantes; }
    public void setHorasRestantes(Double horasRestantes) { this.horasRestantes = horasRestantes; }
    public Double getPercentualConcluido() { return percentualConcluido; }
    public void setPercentualConcluido(Double percentualConcluido) { this.percentualConcluido = percentualConcluido; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

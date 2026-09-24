package br.gov.sp.cpma.desktop.client;

public class ResumoCumprimentoDTO {

    private Long penaId;
    private Long usuarioId;
    private String usuarioNome;
    private Integer horasTotais;
    private Double horasCumpridas;
    private Double horasRestantes;
    private Double percentualConcluido;
    private String status;

    public ResumoCumprimentoDTO() {}

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

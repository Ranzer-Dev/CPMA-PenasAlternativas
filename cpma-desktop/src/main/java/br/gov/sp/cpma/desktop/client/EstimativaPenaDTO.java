package br.gov.sp.cpma.desktop.client;

import java.time.LocalDate;

public class EstimativaPenaDTO {

    private Integer horasTotais;
    private Integer horasSemanais;
    private LocalDate dataInicio;
    private Double tempoEstimadoMeses;
    private LocalDate dataTerminoEstimada;

    public EstimativaPenaDTO() {}

    public EstimativaPenaDTO(Integer horasTotais, Integer horasSemanais, LocalDate dataInicio) {
        this.horasTotais = horasTotais;
        this.horasSemanais = horasSemanais;
        this.dataInicio = dataInicio;
    }

    public Integer getHorasTotais() { return horasTotais; }
    public void setHorasTotais(Integer horasTotais) { this.horasTotais = horasTotais; }
    public Integer getHorasSemanais() { return horasSemanais; }
    public void setHorasSemanais(Integer horasSemanais) { this.horasSemanais = horasSemanais; }
    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
    public Double getTempoEstimadoMeses() { return tempoEstimadoMeses; }
    public void setTempoEstimadoMeses(Double tempoEstimadoMeses) { this.tempoEstimadoMeses = tempoEstimadoMeses; }
    public LocalDate getDataTerminoEstimada() { return dataTerminoEstimada; }
    public void setDataTerminoEstimada(LocalDate dataTerminoEstimada) { this.dataTerminoEstimada = dataTerminoEstimada; }
}

package br.gov.sp.cpma.api.dto;

import java.time.LocalDate;

public class EstimativaPenaResponse {

    private Double tempoEstimadoMeses;
    private LocalDate dataTerminoEstimada;

    public EstimativaPenaResponse() {}

    public EstimativaPenaResponse(Double tempoEstimadoMeses, LocalDate dataTerminoEstimada) {
        this.tempoEstimadoMeses = tempoEstimadoMeses;
        this.dataTerminoEstimada = dataTerminoEstimada;
    }

    public Double getTempoEstimadoMeses() { return tempoEstimadoMeses; }
    public void setTempoEstimadoMeses(Double tempoEstimadoMeses) { this.tempoEstimadoMeses = tempoEstimadoMeses; }
    public LocalDate getDataTerminoEstimada() { return dataTerminoEstimada; }
    public void setDataTerminoEstimada(LocalDate dataTerminoEstimada) { this.dataTerminoEstimada = dataTerminoEstimada; }
}

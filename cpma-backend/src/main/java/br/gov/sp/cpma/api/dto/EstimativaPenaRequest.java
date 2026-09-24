package br.gov.sp.cpma.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class EstimativaPenaRequest {

    @NotNull(message = "Horas totais sao obrigatorias")
    @Min(value = 1, message = "Horas totais devem ser maiores que zero")
    private Integer horasTotais;

    @NotNull(message = "Horas semanais sao obrigatorias")
    @Min(value = 1, message = "Horas semanais devem ser maiores que zero")
    private Integer horasSemanais;

    @NotNull(message = "Data de inicio e obrigatoria")
    private LocalDate dataInicio;

    public Integer getHorasTotais() { return horasTotais; }
    public void setHorasTotais(Integer horasTotais) { this.horasTotais = horasTotais; }
    public Integer getHorasSemanais() { return horasSemanais; }
    public void setHorasSemanais(Integer horasSemanais) { this.horasSemanais = horasSemanais; }
    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
}

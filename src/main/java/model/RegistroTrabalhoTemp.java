package model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Classe temporária para representar um registro de trabalho na tabela.
 * Usada para permitir edição de múltiplos dias antes de salvar no banco.
 */
public class RegistroTrabalhoTemp {
    private LocalDate data;
    private LocalTime horarioInicio;
    private LocalTime horarioAlmoco;
    private LocalTime horarioVolta;
    private LocalTime horarioSaida;
    private double horasCalculadas;

    public RegistroTrabalhoTemp() {
        this.horasCalculadas = 0.0;
    }

    public RegistroTrabalhoTemp(LocalDate data) {
        this.data = data;
        this.horasCalculadas = 0.0;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
        calcularHoras();
    }

    public LocalTime getHorarioInicio() {
        return horarioInicio;
    }

    public void setHorarioInicio(LocalTime horarioInicio) {
        this.horarioInicio = horarioInicio;
        calcularHoras();
    }

    public LocalTime getHorarioAlmoco() {
        return horarioAlmoco;
    }

    public void setHorarioAlmoco(LocalTime horarioAlmoco) {
        this.horarioAlmoco = horarioAlmoco;
        calcularHoras();
    }

    public LocalTime getHorarioVolta() {
        return horarioVolta;
    }

    public void setHorarioVolta(LocalTime horarioVolta) {
        this.horarioVolta = horarioVolta;
        calcularHoras();
    }

    public LocalTime getHorarioSaida() {
        return horarioSaida;
    }

    public void setHorarioSaida(LocalTime horarioSaida) {
        this.horarioSaida = horarioSaida;
        calcularHoras();
    }

    public double getHorasCalculadas() {
        return horasCalculadas;
    }

    /**
     * Calcula as horas cumpridas baseado nos horários de entrada e saída.
     */
    private void calcularHoras() {
        if (horarioInicio != null && horarioAlmoco != null && horarioVolta != null && horarioSaida != null) {
            long minutosManha = java.time.Duration.between(horarioInicio, horarioAlmoco).toMinutes();
            long minutosTarde = java.time.Duration.between(horarioVolta, horarioSaida).toMinutes();
            long totalMinutos = minutosManha + minutosTarde;
            
            if (totalMinutos < 0) {
                horasCalculadas = 0.0;
            } else {
                horasCalculadas = totalMinutos / 60.0;
            }
        } else {
            horasCalculadas = 0.0;
        }
    }

    /**
     * Retorna se o registro está completo e válido para ser salvo.
     */
    public boolean isValid() {
        return data != null && horarioInicio != null && horarioAlmoco != null 
                && horarioVolta != null && horarioSaida != null && horasCalculadas > 0;
    }
}


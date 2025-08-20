package model;

import java.time.LocalTime;

public class DisponibilidadeInstituicao {
    private int idDisponibilidade;
    private String diaSemana;
    private LocalTime horaInicio1;
    private LocalTime horaFim1;
    private LocalTime horaInicio2;
    private LocalTime horaFim2;
    private int fkInstituicaoId;

    public DisponibilidadeInstituicao() {}

    public DisponibilidadeInstituicao(int id, String dia, LocalTime h1, LocalTime f1, LocalTime h2, LocalTime f2) {
        this.idDisponibilidade = id;
        this.diaSemana = dia;
        this.horaInicio1 = h1;
        this.horaFim1 = f1;
        this.horaInicio2 = h2;
        this.horaFim2 = f2;
    }

    public int getIdDisponibilidade() {
        return idDisponibilidade;
    }

    public void setId(int id) {
        this.idDisponibilidade = id;
    }

    public String getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(String diaSemana) {
        this.diaSemana = diaSemana;
    }

    public LocalTime getHoraInicio1() {
        return horaInicio1;
    }

    public void setHoraInicio1(LocalTime horaInicio1) {
        this.horaInicio1 = horaInicio1;
    }

    public LocalTime getHoraFim1() {
        return horaFim1;
    }

    public void setHoraFim1(LocalTime horaFim1) {
        this.horaFim1 = horaFim1;
    }

    public LocalTime getHoraInicio2() {
        return horaInicio2;
    }

    public void setHoraInicio2(LocalTime horaInicio2) {
        this.horaInicio2 = horaInicio2;
    }

    public LocalTime getHoraFim2() {
        return horaFim2;
    }

    public void setHoraFim2(LocalTime horaFim2) {
        this.horaFim2 = horaFim2;
    }

    public int getFkInstituicaoId() {
        return fkInstituicaoId;
    }

    public void setFkInstituicaoId(int fkInstituicaoId) {
        this.fkInstituicaoId = fkInstituicaoId;
    }

    @Override
    public String toString() {
        return diaSemana + " - " +
                (horaInicio1 != null ? horaInicio1 + " às " + horaFim1 : "") +
                (horaInicio2 != null ? ", " + horaInicio2 + " às " + horaFim2 : "");
    }
}

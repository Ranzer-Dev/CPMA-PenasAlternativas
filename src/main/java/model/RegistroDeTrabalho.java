package model;

import java.sql.Date;
import java.sql.Time;

public class RegistroDeTrabalho {
    private int idRegistro;
    private Date dataTrabalho;
    private double horasCumpridas;
    private String atividades;
    private Time horarioInicio;
    private Time horarioAlmoco;
    private Time horarioVolta;
    private Time horarioSaida;
    private int fkPenaId;
    private Date criadoEm;

    public int getIdRegistro() { return idRegistro; }
    public void setIdRegistro(int idRegistro) { this.idRegistro = idRegistro; }

    public Date getDataTrabalho() { return dataTrabalho; }
    public void setDataTrabalho(Date dataTrabalho) { this.dataTrabalho = dataTrabalho; }

    public double getHorasCumpridas() { return horasCumpridas; }
    public void setHorasCumpridas(double horasCumpridas) { this.horasCumpridas = horasCumpridas; }

    public String getAtividades() { return atividades; }
    public void setAtividades(String atividades) { this.atividades = atividades; }

    public Time getHorarioInicio() { return horarioInicio; }
    public void setHorarioInicio(Time horarioInicio) { this.horarioInicio = horarioInicio; }

    public Time getHorarioAlmoco() { return horarioAlmoco; }
    public void setHorarioAlmoco(Time horarioAlmoco) { this.horarioAlmoco = horarioAlmoco; }

    public Time getHorarioVolta() { return horarioVolta; }
    public void setHorarioVolta(Time horarioVolta) { this.horarioVolta = horarioVolta; }

    public Time getHorarioSaida() { return horarioSaida; }
    public void setHorarioSaida(Time horarioSaida) { this.horarioSaida = horarioSaida; }

    public int getFkPenaId() { return fkPenaId; }
    public void setFkPenaId(int fkPenaId) { this.fkPenaId = fkPenaId; }

    public Date getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Date criadoEm) { this.criadoEm = criadoEm; }

}

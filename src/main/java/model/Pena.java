package model;

import java.sql.Date;

public class Pena {
    private int idPena;
    private String tipoPena;
    private Date dataInicio;
    private Date dataTermino;
    private String descricao;
    private String diasSemanaEHorariosDisponivel;
    private String atividadesAcordadas;
    private int horasSemanais;
    private double tempoPena;
    private double horasTotais;
    private int fkUsuarioIdUsuario;
    private int fkInstituicaoIdInstituicao;

    public int getIdPena() {
        return idPena;
    }

    public void setIdPena(int idPena) {
        this.idPena = idPena;
    }

    public String getTipoPena() {
        return tipoPena;
    }

    public void setTipoPena(String tipoPena) {
        this.tipoPena = tipoPena;
    }

    public Date getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(Date dataInicio) {
        this.dataInicio = dataInicio;
    }

    public Date getDataTermino() {
        return dataTermino;
    }

    public void setDataTermino(Date dataTermino) {
        this.dataTermino = dataTermino;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getDiasSemanaEHorariosDisponivel() {
        return diasSemanaEHorariosDisponivel;
    }

    public void setDiasSemanaEHorariosDisponivel(String diasSemanaEHorariosDisponivel) {
        this.diasSemanaEHorariosDisponivel = diasSemanaEHorariosDisponivel;
    }

    public String getAtividadesAcordadas() {
        return atividadesAcordadas;
    }

    public void setAtividadesAcordadas(String atividadesAcordadas) {
        this.atividadesAcordadas = atividadesAcordadas;
    }

    public int getHorasSemanais() {
        return horasSemanais;
    }

    public void setHorasSemanais(int horasSemanais) {
        this.horasSemanais = horasSemanais;
    }

    public double getTempoPena() {
        return tempoPena;
    }

    public void setTempoPena(double tempoPena) {
        this.tempoPena = tempoPena;
    }

    public double getHorasTotais() {
        return horasTotais;
    }

    public void setHorasTotais(double horasTotais) {
        this.horasTotais = horasTotais;
    }

    public int getFkUsuarioIdUsuario() {
        return fkUsuarioIdUsuario;
    }

    public void setFkUsuarioIdUsuario(int fkUsuarioIdUsuario) {
        this.fkUsuarioIdUsuario = fkUsuarioIdUsuario;
    }

    public int getFkInstituicaoIdInstituicao() {
        return fkInstituicaoIdInstituicao;
    }

    public void setFkInstituicaoIdInstituicao(int fkInstituicaoIdInstituicao) {
        this.fkInstituicaoIdInstituicao = fkInstituicaoIdInstituicao;
    }

    @Override
    public String toString() {
        return tipoPena;
    }
}

package model;

public class AcordoDeTrabalho {
    private int idAcordo;
    private String diasSemanaEHorarios;
    private String atividadesAcordadas;
    private int idUsuario;
    private String nomeUsuario;
    private int idInstituicao;
    private String nomeInstituicao;

    public int getIdAcordo() { return idAcordo; }
    public void setIdAcordo(int idAcordo) { this.idAcordo = idAcordo; }

    public String getDiasSemanaEHorarios() { return diasSemanaEHorarios; }
    public void setDiasSemanaEHorarios(String diasSemanaEHorarios) { this.diasSemanaEHorarios = diasSemanaEHorarios; }

    public String getAtividadesAcordadas() { return atividadesAcordadas; }
    public void setAtividadesAcordadas(String atividadesAcordadas) { this.atividadesAcordadas = atividadesAcordadas; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public void setNomeUsuario(String n){ this.nomeUsuario = n; }
    public String getNomeUsuario(){ return nomeUsuario; }

    public int getIdInstituicao() { return idInstituicao; }
    public void setIdInstituicao(int idInstituicao) { this.idInstituicao = idInstituicao; }

    public String getNomeInstituicao() { return nomeInstituicao; }
    public void setNomeInstituicao(String nomeInstituicao) { this.nomeInstituicao = nomeInstituicao; }

    @Override
    public String toString() {
        return nomeUsuario == null
                ? "Acordo " + idAcordo
                : nomeUsuario + " (acordo " + idAcordo + ")";
    }
}

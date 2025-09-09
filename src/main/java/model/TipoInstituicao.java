package model;

public class TipoInstituicao {
    private int idTipo;
    private String tipo;

    public TipoInstituicao() {
    }

    public TipoInstituicao(int idTipo, String tipo) {
        this.idTipo = idTipo;
        this.tipo = tipo;
    }

    public int getIdTipo() {
        return idTipo;
    }

    public void setId(int id) {
        this.idTipo = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return tipo;
    }
}

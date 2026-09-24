package br.gov.sp.cpma.desktop.client;

public class TipoInstituicaoDTO {

    private Long idTipo;
    private String tipo;

    public TipoInstituicaoDTO() {}

    public TipoInstituicaoDTO(Long idTipo, String tipo) {
        this.idTipo = idTipo;
        this.tipo = tipo;
    }

    public Long getIdTipo() { return idTipo; }
    public void setIdTipo(Long idTipo) { this.idTipo = idTipo; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    @Override
    public String toString() {
        return tipo != null ? tipo : "";
    }
}

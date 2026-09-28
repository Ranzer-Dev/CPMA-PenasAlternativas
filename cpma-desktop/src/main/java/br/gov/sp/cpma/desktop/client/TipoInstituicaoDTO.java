package br.gov.sp.cpma.desktop.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TipoInstituicaoDTO {

    private Long idTipo;
    private String tipo;
    private LocalDateTime criadoEm;

    public TipoInstituicaoDTO() {}

    public TipoInstituicaoDTO(Long idTipo, String tipo) {
        this.idTipo = idTipo;
        this.tipo = tipo;
    }

    public TipoInstituicaoDTO(Long idTipo, String tipo, LocalDateTime criadoEm) {
        this.idTipo = idTipo;
        this.tipo = tipo;
        this.criadoEm = criadoEm;
    }

    public Long getIdTipo() { return idTipo; }
    public void setIdTipo(Long idTipo) { this.idTipo = idTipo; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    @Override
    public String toString() {
        return tipo != null ? tipo : "";
    }
}


package br.gov.sp.cpma.api.dto;

import br.gov.sp.cpma.domain.entity.TipoInstituicao;
import java.time.LocalDateTime;

public class TipoInstituicaoResponse {

    private Long idTipo;
    private String tipo;
    private LocalDateTime criadoEm;

    public TipoInstituicaoResponse() {}

    public TipoInstituicaoResponse(TipoInstituicao t) {
        this.idTipo = t.getIdTipo();
        this.tipo = t.getTipo();
        this.criadoEm = t.getCriadoEm();
    }

    public Long getIdTipo() { return idTipo; }
    public void setIdTipo(Long idTipo) { this.idTipo = idTipo; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}

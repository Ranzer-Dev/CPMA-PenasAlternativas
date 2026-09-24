package br.gov.sp.cpma.api.dto;

import jakarta.validation.constraints.NotBlank;

public class CadastroTipoInstituicaoRequest {

    @NotBlank(message = "Tipo e obrigatorio")
    private String tipo;

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}

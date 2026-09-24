package br.gov.sp.cpma.api.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "CPF e obrigatorio")
    private String cpf;

    @NotBlank(message = "Senha e obrigatoria")
    private String senha;

    public LoginRequest() {}

    public LoginRequest(String cpf, String senha) {
        this.cpf = cpf;
        this.senha = senha;
    }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}

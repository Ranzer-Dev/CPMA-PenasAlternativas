package br.gov.sp.cpma.api.dto;

import jakarta.validation.constraints.NotBlank;

public class RedefinirSenhaRequest {

    @NotBlank(message = "CPF e obrigatorio")
    private String cpf;

    @NotBlank(message = "Resposta secreta e obrigatoria")
    private String respostaSecreta;

    @NotBlank(message = "Nova senha e obrigatoria")
    private String novaSenha;

    public RedefinirSenhaRequest() {}

    public RedefinirSenhaRequest(String cpf, String respostaSecreta, String novaSenha) {
        this.cpf = cpf;
        this.respostaSecreta = respostaSecreta;
        this.novaSenha = novaSenha;
    }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getRespostaSecreta() { return respostaSecreta; }
    public void setRespostaSecreta(String respostaSecreta) { this.respostaSecreta = respostaSecreta; }
    public String getNovaSenha() { return novaSenha; }
    public void setNovaSenha(String novaSenha) { this.novaSenha = novaSenha; }
}

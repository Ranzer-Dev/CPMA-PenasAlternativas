package br.gov.sp.cpma.api.dto;

public class LoginResponse {

    private Long adminId;
    private String nome;
    private String cpf;
    private Integer nivelPermissao;
    private String token;

    public LoginResponse() {}

    public LoginResponse(Long adminId, String nome, String cpf, Integer nivelPermissao, String token) {
        this.adminId = adminId;
        this.nome = nome;
        this.cpf = cpf;
        this.nivelPermissao = nivelPermissao;
        this.token = token;
    }

    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public Integer getNivelPermissao() { return nivelPermissao; }
    public void setNivelPermissao(Integer nivelPermissao) { this.nivelPermissao = nivelPermissao; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}

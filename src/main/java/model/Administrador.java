package model;

public class Administrador {
    private int idAdministrador;
    private String nome;
    private String cpf;
    private String senha;
    private int nivelPermissao;
    private String perguntaSecreta;
    private String respostaSecreta;

    public int getIdAdministrador() {
        return idAdministrador;
    }

    public void setIdAdministrador(int id) {
        this.idAdministrador = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public int getNivelPermissao() {
        return nivelPermissao;
    }

    public void setNivelPermissao(int nivelPermissao) {
        this.nivelPermissao = nivelPermissao;
    }

    public String getPerguntaSecreta() {
        return perguntaSecreta;
    }

    public void setPerguntaSecreta(String perguntaSecreta) {
        this.perguntaSecreta = perguntaSecreta;
    }

    public String getRespostaSecreta() {
        return respostaSecreta;
    }

    public void setRespostaSecreta(String respostaSecreta) {
        this.respostaSecreta = respostaSecreta;
    }
}

package model;

import java.util.Date;

public class Usuario {
    private int idUsuario;
    private String codigo;
    private String nome;
    private String cpf;
    private Date dataNascimento;
    private String endereco;
    private String cep;
    private String bairro;
    private String cidade;
    private String uf;
    private String telefone;
    private String nacionalidade;
    private Date criadoEm;
    private String foto;
    private String senha;
    private String observacao;
    private int fkAdministradorIdAdmin;

    public int getIdUsuario() {
        return idUsuario;
    }
    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getCodigo() {return codigo;}
    public void setCodigo(String codigo) {this.codigo = codigo;}

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

    public Date getDataNascimento() {
        return dataNascimento;
    }
    public void setDataNascimento(Date dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getEndereco() {
        return endereco;
    }
    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getCep() {return cep;}
    public void setCep(String cep) { this.cep = cep; }

    public String getBairro() {
        return bairro;
    }
    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }
    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getUf() {
        return uf;
    }
    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getTelefone() {return telefone;}
    public void setTelefone(String telefone) {this.telefone = telefone;}

    public String getNacionalidade() {
        return nacionalidade;
    }
    public void setNacionalidade(String nacionalidade) {
        this.nacionalidade = nacionalidade;
    }

    public Date getCriadoEm() {
        return criadoEm;
    }
    public void setCriadoEm(Date criadoEm) {
        this.criadoEm = criadoEm;
    }

    public String getFoto() {
        return foto;
    }
    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getSenha() {
        return senha;
    }
    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getObservacao() {
        return observacao;
    }
    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public int getFkAdministradorIdAdmin() {
        return fkAdministradorIdAdmin;
    }
    public void setFkAdministradorIdAdmin(int fkAdministradorIdAdmin) {
        this.fkAdministradorIdAdmin = fkAdministradorIdAdmin;
    }

    @Override
    public String toString() {
        return nome;
    }
}

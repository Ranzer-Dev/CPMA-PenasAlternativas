package model;

public class Instituicao {
    private int idInstituicao;
    private String nome;
    private String endereco;
    private String cidade;
    private String uf;
    private String bairro;
    private String cep;
    private String responsavel;
    private String responsavel2;
    private String telefone;
    private String telefone2;
    private int tipo;
    private java.util.Date criadoEm;

    public int getIdInstituicao() { return idInstituicao; }
    public void setIdInstituicao(int idInstituicao) { this.idInstituicao = idInstituicao; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }

    public String getResponsavel2() {return responsavel2;}
    public void setResponsavel2(String responsavel2) {this.responsavel2 = responsavel2;}

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getTelefone2() {return telefone2;}
    public void setTelefone2(String telefone2) {this.telefone2 = telefone2;}

    public int getTipo() { return tipo; }
    public void setTipo(int tipo) { this.tipo = tipo; }

    public java.util.Date getCriadoEm() { return criadoEm; }
    public void setCriadoEm(java.util.Date criadoEm) { this.criadoEm = criadoEm; }

    @Override
    public String toString() { return nome; }
}

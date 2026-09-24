package br.gov.sp.cpma.desktop.client;

import java.util.ArrayList;
import java.util.List;

public class CadastroInstituicaoDTO {

    private String nome;
    private String endereco;
    private String cidade;
    private String uf;
    private String bairro;
    private String cep;
    private String responsavel;
    private String telefone;
    private Long tipoId;
    private List<DisponibilidadeItemDTO> disponibilidades = new ArrayList<>();

    public CadastroInstituicaoDTO() {}

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
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public Long getTipoId() { return tipoId; }
    public void setTipoId(Long tipoId) { this.tipoId = tipoId; }
    public List<DisponibilidadeItemDTO> getDisponibilidades() { return disponibilidades; }
    public void setDisponibilidades(List<DisponibilidadeItemDTO> disponibilidades) { this.disponibilidades = disponibilidades; }
}

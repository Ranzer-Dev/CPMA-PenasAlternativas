package br.gov.sp.cpma.desktop.client;

import java.util.ArrayList;
import java.util.List;

public class InstituicaoDTO {

    private Long idInstituicao;
    private String nome;
    private String endereco;
    private String cidade;
    private String uf;
    private String bairro;
    private String cep;
    private String responsavel;
    private String telefone;
    private Long tipoId;
    private String tipoNome;
    private List<DisponibilidadeItemDTO> disponibilidades = new ArrayList<>();

    public InstituicaoDTO() {}

    public Long getIdInstituicao() { return idInstituicao; }
    public void setIdInstituicao(Long idInstituicao) { this.idInstituicao = idInstituicao; }
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
    public String getTipoNome() { return tipoNome; }
    public void setTipoNome(String tipoNome) { this.tipoNome = tipoNome; }
    public List<DisponibilidadeItemDTO> getDisponibilidades() { return disponibilidades; }
    public void setDisponibilidades(List<DisponibilidadeItemDTO> disponibilidades) { this.disponibilidades = disponibilidades; }

    @Override
    public String toString() {
        return nome != null ? nome : "";
    }
}

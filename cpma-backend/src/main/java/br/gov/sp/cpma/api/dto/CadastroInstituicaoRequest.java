package br.gov.sp.cpma.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class CadastroInstituicaoRequest {

    @NotBlank(message = "Nome da instituicao e obrigatorio")
    private String nome;

    private String endereco;
    private String cidade;
    private String uf;
    private String bairro;
    private String cep;
    private String responsavel;
    private String telefone;

    @NotNull(message = "Tipo de instituicao e obrigatorio")
    private Long tipoId;

    private List<DisponibilidadeDTO> disponibilidades;

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
    public List<DisponibilidadeDTO> getDisponibilidades() { return disponibilidades; }
    public void setDisponibilidades(List<DisponibilidadeDTO> disponibilidades) { this.disponibilidades = disponibilidades; }
}

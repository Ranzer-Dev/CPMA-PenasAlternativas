package br.gov.sp.cpma.api.dto;

import br.gov.sp.cpma.domain.entity.Instituicao;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InstituicaoResponse {

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
    private LocalDateTime criadoEm;
    private List<DisponibilidadeDTO> disponibilidades = new ArrayList<>();

    public InstituicaoResponse() {}

    public InstituicaoResponse(Instituicao i) {
        this.idInstituicao = i.getIdInstituicao();
        this.nome = i.getNome();
        this.endereco = i.getEndereco();
        this.cidade = i.getCidade();
        this.uf = i.getUf();
        this.bairro = i.getBairro();
        this.cep = i.getCep();
        this.responsavel = i.getResponsavel();
        this.telefone = i.getTelefone();
        if (i.getTipo() != null) {
            this.tipoId = i.getTipo().getIdTipo();
            this.tipoNome = i.getTipo().getTipo();
        }
        this.criadoEm = i.getCriadoEm();
    }

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
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public List<DisponibilidadeDTO> getDisponibilidades() { return disponibilidades; }
    public void setDisponibilidades(List<DisponibilidadeDTO> disponibilidades) { this.disponibilidades = disponibilidades; }
}

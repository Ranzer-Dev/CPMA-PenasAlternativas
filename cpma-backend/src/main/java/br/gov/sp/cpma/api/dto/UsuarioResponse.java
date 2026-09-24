package br.gov.sp.cpma.api.dto;

import br.gov.sp.cpma.domain.entity.Usuario;
import java.time.LocalDateTime;

public class UsuarioResponse {

    private Long idUsuario;
    private String codigo;
    private String nome;
    private String cpf;
    private String dataNascimento;
    private String endereco;
    private String bairro;
    private String cidade;
    private String cep;
    private String uf;
    private String nacionalidade;
    private String foto;
    private String observacao;
    private String telefone;
    private LocalDateTime criadoEm;

    public UsuarioResponse() {}

    public UsuarioResponse(Usuario usuario) {
        this.idUsuario = usuario.getIdUsuario();
        this.codigo = usuario.getCodigo();
        this.nome = usuario.getNome();
        this.cpf = usuario.getCpf();
        this.dataNascimento = usuario.getDataNascimento();
        this.endereco = usuario.getEndereco();
        this.bairro = usuario.getBairro();
        this.cidade = usuario.getCidade();
        this.cep = usuario.getCep();
        this.uf = usuario.getUf();
        this.nacionalidade = usuario.getNacionalidade();
        this.foto = usuario.getFoto();
        this.observacao = usuario.getObservacao();
        this.telefone = usuario.getTelefone();
        this.criadoEm = usuario.getCriadoEm();
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public String getCpf() {
        return cpf;
    }

    public String getDataNascimento() {
        return dataNascimento;
    }

    public String getEndereco() {
        return endereco;
    }

    public String getBairro() {
        return bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public String getCep() {
        return cep;
    }

    public String getUf() {
        return uf;
    }

    public String getNacionalidade() {
        return nacionalidade;
    }

    public String getFoto() {
        return foto;
    }

    public String getObservacao() {
        return observacao;
    }

    public String getTelefone() {
        return telefone;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}

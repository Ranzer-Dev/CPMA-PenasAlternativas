package br.gov.sp.cpma.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Administrador")
public class Administrador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_admin")
    private Long idAdmin;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "cpf", nullable = false, unique = true)
    private String cpf;

    @Column(name = "senha", nullable = false)
    private String senha;

    @Column(name = "nivel_permissao", nullable = false)
    private Integer nivelPermissao;

    @Column(name = "pergunta_secreta")
    private String perguntaSecreta;

    @Column(name = "resposta_secreta")
    private String respostaSecreta;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    public void prePersist() {
        if (this.criadoEm == null) {
            this.criadoEm = LocalDateTime.now();
        }
    }

    public Long getIdAdmin() {
        return idAdmin;
    }

    public void setIdAdmin(Long idAdmin) {
        this.idAdmin = idAdmin;
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

    public Integer getNivelPermissao() {
        return nivelPermissao;
    }

    public void setNivelPermissao(Integer nivelPermissao) {
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

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}

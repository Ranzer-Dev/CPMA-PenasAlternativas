package br.gov.sp.cpma.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "CodigoAcessoApenado")
public class CodigoAcessoApenado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_codigo_acesso")
    private Long idCodigoAcesso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_usuario_id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_admin_id_admin", nullable = false)
    private Administrador administrador;

    @Column(name = "codigo", nullable = false)
    private String codigo;

    @Column(name = "data_geracao", nullable = false)
    private LocalDateTime dataGeracao;

    @Column(name = "data_expiracao", nullable = false)
    private LocalDateTime dataExpiracao;

    @Column(name = "data_uso")
    private LocalDateTime dataUso;

    @Column(name = "status", nullable = false)
    private String status;

    @PrePersist
    public void prePersist() {
        if (this.dataGeracao == null) {
            this.dataGeracao = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = "ATIVO";
        }
    }

    public Long getIdCodigoAcesso() {
        return idCodigoAcesso;
    }

    public void setIdCodigoAcesso(Long idCodigoAcesso) {
        this.idCodigoAcesso = idCodigoAcesso;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Administrador getAdministrador() {
        return administrador;
    }

    public void setAdministrador(Administrador administrador) {
        this.administrador = administrador;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public LocalDateTime getDataGeracao() {
        return dataGeracao;
    }

    public void setDataGeracao(LocalDateTime dataGeracao) {
        this.dataGeracao = dataGeracao;
    }

    public LocalDateTime getDataExpiracao() {
        return dataExpiracao;
    }

    public void setDataExpiracao(LocalDateTime dataExpiracao) {
        this.dataExpiracao = dataExpiracao;
    }

    public LocalDateTime getDataUso() {
        return dataUso;
    }

    public void setDataUso(LocalDateTime dataUso) {
        this.dataUso = dataUso;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

package br.gov.sp.cpma.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "LogAcessoApenado")
public class LogAcessoApenado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    private Long idLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_usuario_id_usuario")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_codigo_acesso_id")
    private CodigoAcessoApenado codigoAcesso;

    @Column(name = "metodo", nullable = false)
    private String metodo;

    @Column(name = "sucesso", nullable = false)
    private Integer sucesso;

    @Column(name = "codigo_tentado")
    private String codigoTentado;

    @Column(name = "mensagem")
    private String mensagem;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @PrePersist
    public void prePersist() {
        if (this.dataHora == null) {
            this.dataHora = LocalDateTime.now();
        }
    }

    public Long getIdLog() {
        return idLog;
    }

    public void setIdLog(Long idLog) {
        this.idLog = idLog;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public CodigoAcessoApenado getCodigoAcesso() {
        return codigoAcesso;
    }

    public void setCodigoAcesso(CodigoAcessoApenado codigoAcesso) {
        this.codigoAcesso = codigoAcesso;
    }

    public String getMetodo() {
        return metodo;
    }

    public void setMetodo(String metodo) {
        this.metodo = metodo;
    }

    public Integer getSucesso() {
        return sucesso;
    }

    public void setSucesso(Integer sucesso) {
        this.sucesso = sucesso;
    }

    public String getCodigoTentado() {
        return codigoTentado;
    }

    public void setCodigoTentado(String codigoTentado) {
        this.codigoTentado = codigoTentado;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
}

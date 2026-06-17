package model;

import java.time.LocalDateTime;

/**
 * Código temporário emitido pelo delegado para o apenado acessar
 * seus registros no totem sem depender do reconhecimento facial.
 */
public class CodigoAcessoApenado {

    public static final String STATUS_ATIVO = "ATIVO";
    public static final String STATUS_USADO = "USADO";
    public static final String STATUS_EXPIRADO = "EXPIRADO";
    public static final String STATUS_CANCELADO = "CANCELADO";

    private int idCodigoAcesso;
    private int fkUsuarioIdUsuario;
    private int fkAdminIdAdmin;
    private String codigo;
    private LocalDateTime dataGeracao;
    private LocalDateTime dataExpiracao;
    private LocalDateTime dataUso;
    private String status;

    public CodigoAcessoApenado() {
    }

    public int getIdCodigoAcesso() {
        return idCodigoAcesso;
    }

    public void setIdCodigoAcesso(int idCodigoAcesso) {
        this.idCodigoAcesso = idCodigoAcesso;
    }

    public int getFkUsuarioIdUsuario() {
        return fkUsuarioIdUsuario;
    }

    public void setFkUsuarioIdUsuario(int fkUsuarioIdUsuario) {
        this.fkUsuarioIdUsuario = fkUsuarioIdUsuario;
    }

    public int getFkAdminIdAdmin() {
        return fkAdminIdAdmin;
    }

    public void setFkAdminIdAdmin(int fkAdminIdAdmin) {
        this.fkAdminIdAdmin = fkAdminIdAdmin;
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

    public boolean estaExpirado() {
        return dataExpiracao != null && LocalDateTime.now().isAfter(dataExpiracao);
    }

    public boolean estaUtilizavel() {
        return STATUS_ATIVO.equals(status) && !estaExpirado();
    }
}

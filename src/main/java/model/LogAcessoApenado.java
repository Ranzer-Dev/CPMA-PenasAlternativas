package model;

import java.time.LocalDateTime;

/**
 * Registro de auditoria das tentativas de acesso ao totem,
 * por código de acesso ou reconhecimento facial.
 */
public class LogAcessoApenado {

    public static final String METODO_CODIGO = "CODIGO";
    public static final String METODO_FACIAL = "FACIAL";

    private int idLog;
    private Integer fkUsuarioIdUsuario;
    private Integer fkCodigoAcessoId;
    private String metodo;
    private boolean sucesso;
    private String codigoTentado;
    private String mensagem;
    private LocalDateTime dataHora;

    public LogAcessoApenado() {
    }

    public int getIdLog() {
        return idLog;
    }

    public void setIdLog(int idLog) {
        this.idLog = idLog;
    }

    public Integer getFkUsuarioIdUsuario() {
        return fkUsuarioIdUsuario;
    }

    public void setFkUsuarioIdUsuario(Integer fkUsuarioIdUsuario) {
        this.fkUsuarioIdUsuario = fkUsuarioIdUsuario;
    }

    public Integer getFkCodigoAcessoId() {
        return fkCodigoAcessoId;
    }

    public void setFkCodigoAcessoId(Integer fkCodigoAcessoId) {
        this.fkCodigoAcessoId = fkCodigoAcessoId;
    }

    public String getMetodo() {
        return metodo;
    }

    public void setMetodo(String metodo) {
        this.metodo = metodo;
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public void setSucesso(boolean sucesso) {
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

package model;

import java.sql.Blob;
import java.sql.Date;

public class DadosFaciais {

    private int idDadosFaciais;
    private int fkUsuarioIdUsuario;
    private Blob imagemRosto;
    private String descritoresFaciais; // JSON com os descritores faciais
    private Date dataCadastro;
    private Date dataAtualizacao;
    private boolean ativo;

    // Construtor padrão
    public DadosFaciais() {
    }

    // Construtor com parâmetros
    public DadosFaciais(int fkUsuarioIdUsuario, Blob imagemRosto, String descritoresFaciais) {
        this.fkUsuarioIdUsuario = fkUsuarioIdUsuario;
        this.imagemRosto = imagemRosto;
        this.descritoresFaciais = descritoresFaciais;
        this.dataCadastro = new Date(System.currentTimeMillis());
        this.dataAtualizacao = new Date(System.currentTimeMillis());
        this.ativo = true;
    }

    // Getters e Setters
    public int getIdDadosFaciais() {
        return idDadosFaciais;
    }

    public void setIdDadosFaciais(int idDadosFaciais) {
        this.idDadosFaciais = idDadosFaciais;
    }

    public int getFkUsuarioIdUsuario() {
        return fkUsuarioIdUsuario;
    }

    public void setFkUsuarioIdUsuario(int fkUsuarioIdUsuario) {
        this.fkUsuarioIdUsuario = fkUsuarioIdUsuario;
    }

    public Blob getImagemRosto() {
        return imagemRosto;
    }

    public void setImagemRosto(Blob imagemRosto) {
        this.imagemRosto = imagemRosto;
    }

    public String getDescritoresFaciais() {
        return descritoresFaciais;
    }

    public void setDescritoresFaciais(String descritoresFaciais) {
        this.descritoresFaciais = descritoresFaciais;
    }

    public Date getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(Date dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public Date getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(Date dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    @Override
    public String toString() {
        return "DadosFaciais{"
                + "idDadosFaciais=" + idDadosFaciais
                + ", fkUsuarioIdUsuario=" + fkUsuarioIdUsuario
                + ", dataCadastro=" + dataCadastro
                + ", ativo=" + ativo
                + '}';
    }
}

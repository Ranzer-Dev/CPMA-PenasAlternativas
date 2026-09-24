package br.gov.sp.cpma.api.dto;

import br.gov.sp.cpma.domain.entity.Instituicao;
import br.gov.sp.cpma.domain.entity.Pena;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PenaResponse {

    private Long idPena;
    private Long usuarioId;
    private String usuarioNome;
    private Long instituicaoPrincipalId;
    private String instituicaoPrincipalNome;
    private String tipoPena;
    private LocalDate dataInicio;
    private LocalDate dataTermino;
    private String descricao;
    private String diasSemanaEHorariosDisponivel;
    private String atividadesAcordadas;
    private Integer horasSemanais;
    private Double tempoPena;
    private Integer horasTotais;
    private String codigoAtualUsuario;
    private LocalDateTime criadoEm;
    private List<Long> instituicoesVinculadasIds = new ArrayList<>();

    public PenaResponse() {}

    public PenaResponse(Pena p) {
        this.idPena = p.getIdPena();
        if (p.getUsuario() != null) {
            this.usuarioId = p.getUsuario().getIdUsuario();
            this.usuarioNome = p.getUsuario().getNome();
            this.codigoAtualUsuario = p.getUsuario().getCodigo();
        }
        if (p.getInstituicaoPrincipal() != null) {
            this.instituicaoPrincipalId = p.getInstituicaoPrincipal().getIdInstituicao();
            this.instituicaoPrincipalNome = p.getInstituicaoPrincipal().getNome();
        }
        this.tipoPena = p.getTipoPena();
        this.dataInicio = p.getDataInicio();
        this.dataTermino = p.getDataTermino();
        this.descricao = p.getDescricao();
        this.diasSemanaEHorariosDisponivel = p.getDiasSemanaEHorariosDisponivel();
        this.atividadesAcordadas = p.getAtividadesAcordadas();
        this.horasSemanais = p.getHorasSemanais();
        this.tempoPena = p.getTempoPena();
        this.horasTotais = p.getHorasTotais();
        this.criadoEm = p.getCriadoEm();
        if (p.getInstituicoesVinculadas() != null) {
            for (Instituicao inst : p.getInstituicoesVinculadas()) {
                this.instituicoesVinculadasIds.add(inst.getIdInstituicao());
            }
        }
    }

    public Long getIdPena() { return idPena; }
    public void setIdPena(Long idPena) { this.idPena = idPena; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getUsuarioNome() { return usuarioNome; }
    public void setUsuarioNome(String usuarioNome) { this.usuarioNome = usuarioNome; }
    public Long getInstituicaoPrincipalId() { return instituicaoPrincipalId; }
    public void setInstituicaoPrincipalId(Long instituicaoPrincipalId) { this.instituicaoPrincipalId = instituicaoPrincipalId; }
    public String getInstituicaoPrincipalNome() { return instituicaoPrincipalNome; }
    public void setInstituicaoPrincipalNome(String instituicaoPrincipalNome) { this.instituicaoPrincipalNome = instituicaoPrincipalNome; }
    public String getTipoPena() { return tipoPena; }
    public void setTipoPena(String tipoPena) { this.tipoPena = tipoPena; }
    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
    public LocalDate getDataTermino() { return dataTermino; }
    public void setDataTermino(LocalDate dataTermino) { this.dataTermino = dataTermino; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getDiasSemanaEHorariosDisponivel() { return diasSemanaEHorariosDisponivel; }
    public void setDiasSemanaEHorariosDisponivel(String diasSemanaEHorariosDisponivel) { this.diasSemanaEHorariosDisponivel = diasSemanaEHorariosDisponivel; }
    public String getAtividadesAcordadas() { return atividadesAcordadas; }
    public void setAtividadesAcordadas(String atividadesAcordadas) { this.atividadesAcordadas = atividadesAcordadas; }
    public Integer getHorasSemanais() { return horasSemanais; }
    public void setHorasSemanais(Integer horasSemanais) { this.horasSemanais = horasSemanais; }
    public Double getTempoPena() { return tempoPena; }
    public void setTempoPena(Double tempoPena) { this.tempoPena = tempoPena; }
    public Integer getHorasTotais() { return horasTotais; }
    public void setHorasTotais(Integer horasTotais) { this.horasTotais = horasTotais; }
    public String getCodigoAtualUsuario() { return codigoAtualUsuario; }
    public void setCodigoAtualUsuario(String codigoAtualUsuario) { this.codigoAtualUsuario = codigoAtualUsuario; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public List<Long> getInstituicoesVinculadasIds() { return instituicoesVinculadasIds; }
    public void setInstituicoesVinculadasIds(List<Long> instituicoesVinculadasIds) { this.instituicoesVinculadasIds = instituicoesVinculadasIds; }
}

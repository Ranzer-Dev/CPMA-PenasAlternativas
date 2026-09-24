package br.gov.sp.cpma.desktop.client;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CadastroPenaDTO {

    private Long usuarioId;
    private Long instituicaoId;
    private String tipoPena;
    private LocalDate dataInicio;
    private LocalDate dataTermino;
    private String descricao;
    private String diasSemanaEHorariosDisponivel;
    private String atividadesAcordadas;
    private Integer horasSemanais;
    private Integer horasTotais;
    private List<Long> outrasInstituicoesIds = new ArrayList<>();

    public CadastroPenaDTO() {}

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public Long getInstituicaoId() { return instituicaoId; }
    public void setInstituicaoId(Long instituicaoId) { this.instituicaoId = instituicaoId; }
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
    public Integer getHorasTotais() { return horasTotais; }
    public void setHorasTotais(Integer horasTotais) { this.horasTotais = horasTotais; }
    public List<Long> getOutrasInstituicoesIds() { return outrasInstituicoesIds; }
    public void setOutrasInstituicoesIds(List<Long> outrasInstituicoesIds) { this.outrasInstituicoesIds = outrasInstituicoesIds; }
}

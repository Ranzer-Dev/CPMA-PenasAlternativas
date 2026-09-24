package br.gov.sp.cpma.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public class CadastroPenaRequest {

    @NotNull(message = "ID do usuario e obrigatorio")
    private Long usuarioId;

    @NotNull(message = "ID da instituicao principal e obrigatorio")
    private Long instituicaoId;

    @NotBlank(message = "Tipo de pena e obrigatorio")
    private String tipoPena;

    @NotNull(message = "Data de inicio e obrigatoria")
    private LocalDate dataInicio;

    private LocalDate dataTermino;
    private String descricao;
    private String diasSemanaEHorariosDisponivel;
    private String atividadesAcordadas;

    @NotNull(message = "Horas semanais sao obrigatorias")
    @Min(value = 1, message = "Horas semanais devem ser maiores que zero")
    private Integer horasSemanais;

    @NotNull(message = "Horas totais sao obrigatorias")
    @Min(value = 1, message = "Horas totais devem ser maiores que zero")
    private Integer horasTotais;

    private List<Long> outrasInstituicoesIds;

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

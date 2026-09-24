package br.gov.sp.cpma.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Pena")
public class Pena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pena")
    private Long idPena;

    @Column(name = "tipo_pena", nullable = false)
    private String tipoPena;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_termino")
    private LocalDate dataTermino;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "dias_semana_e_horarios_disponivel")
    private String diasSemanaEHorariosDisponivel;

    @Column(name = "atividades_acordadas")
    private String atividadesAcordadas;

    @Column(name = "horas_semanais", nullable = false)
    private Integer horasSemanais;

    @Column(name = "tempo_pena", nullable = false)
    private Double tempoPena;

    @Column(name = "horas_totais", nullable = false)
    private Integer horasTotais;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_usuario_id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_instituicao_id_instituicao", nullable = false)
    private Instituicao instituicaoPrincipal;

    @ManyToMany
    @JoinTable(
        name = "pena_instituicao",
        joinColumns = @JoinColumn(name = "fk_pena_id_pena"),
        inverseJoinColumns = @JoinColumn(name = "fk_instituicao_id_instituicao")
    )
    private List<Instituicao> instituicoesVinculadas = new ArrayList<>();

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    public void prePersist() {
        if (this.criadoEm == null) {
            this.criadoEm = LocalDateTime.now();
        }
    }

    public Long getIdPena() {
        return idPena;
    }

    public void setIdPena(Long idPena) {
        this.idPena = idPena;
    }

    public String getTipoPena() {
        return tipoPena;
    }

    public void setTipoPena(String tipoPena) {
        this.tipoPena = tipoPena;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataTermino() {
        return dataTermino;
    }

    public void setDataTermino(LocalDate dataTermino) {
        this.dataTermino = dataTermino;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getDiasSemanaEHorariosDisponivel() {
        return diasSemanaEHorariosDisponivel;
    }

    public void setDiasSemanaEHorariosDisponivel(String diasSemanaEHorariosDisponivel) {
        this.diasSemanaEHorariosDisponivel = diasSemanaEHorariosDisponivel;
    }

    public String getAtividadesAcordadas() {
        return atividadesAcordadas;
    }

    public void setAtividadesAcordadas(String atividadesAcordadas) {
        this.atividadesAcordadas = atividadesAcordadas;
    }

    public Integer getHorasSemanais() {
        return horasSemanais;
    }

    public void setHorasSemanais(Integer horasSemanais) {
        this.horasSemanais = horasSemanais;
    }

    public Double getTempoPena() {
        return tempoPena;
    }

    public void setTempoPena(Double tempoPena) {
        this.tempoPena = tempoPena;
    }

    public Integer getHorasTotais() {
        return horasTotais;
    }

    public void setHorasTotais(Integer horasTotais) {
        this.horasTotais = horasTotais;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Instituicao getInstituicaoPrincipal() {
        return instituicaoPrincipal;
    }

    public void setInstituicaoPrincipal(Instituicao instituicaoPrincipal) {
        this.instituicaoPrincipal = instituicaoPrincipal;
    }

    public List<Instituicao> getInstituicoesVinculadas() {
        return instituicoesVinculadas;
    }

    public void setInstituicoesVinculadas(List<Instituicao> instituicoesVinculadas) {
        this.instituicoesVinculadas = instituicoesVinculadas;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}

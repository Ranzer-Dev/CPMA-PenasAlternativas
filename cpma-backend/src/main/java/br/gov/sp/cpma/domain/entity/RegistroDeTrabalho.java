package br.gov.sp.cpma.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "RegistroDeTrabalho")
public class RegistroDeTrabalho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_registro")
    private Long idRegistro;

    @Column(name = "data_trabalho", nullable = false)
    private LocalDate dataTrabalho;

    @Column(name = "horas_cumpridas", nullable = false)
    private Double horasCumpridas;

    @Column(name = "atividades")
    private String atividades;

    @Column(name = "horario_inicio")
    private String horarioInicio;

    @Column(name = "horario_almoco")
    private String horarioAlmoco;

    @Column(name = "horario_volta")
    private String horarioVolta;

    @Column(name = "horario_saida")
    private String horarioSaida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_pena_id_pena", nullable = false)
    private Pena pena;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_instituicao_id_instituicao")
    private Instituicao instituicao;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    public void prePersist() {
        if (this.criadoEm == null) {
            this.criadoEm = LocalDateTime.now();
        }
    }

    public Long getIdRegistro() {
        return idRegistro;
    }

    public void setIdRegistro(Long idRegistro) {
        this.idRegistro = idRegistro;
    }

    public LocalDate getDataTrabalho() {
        return dataTrabalho;
    }

    public void setDataTrabalho(LocalDate dataTrabalho) {
        this.dataTrabalho = dataTrabalho;
    }

    public Double getHorasCumpridas() {
        return horasCumpridas;
    }

    public void setHorasCumpridas(Double horasCumpridas) {
        this.horasCumpridas = horasCumpridas;
    }

    public String getAtividades() {
        return atividades;
    }

    public void setAtividades(String atividades) {
        this.atividades = atividades;
    }

    public String getHorarioInicio() {
        return horarioInicio;
    }

    public void setHorarioInicio(String horarioInicio) {
        this.horarioInicio = horarioInicio;
    }

    public String getHorarioAlmoco() {
        return horarioAlmoco;
    }

    public void setHorarioAlmoco(String horarioAlmoco) {
        this.horarioAlmoco = horarioAlmoco;
    }

    public String getHorarioVolta() {
        return horarioVolta;
    }

    public void setHorarioVolta(String horarioVolta) {
        this.horarioVolta = horarioVolta;
    }

    public String getHorarioSaida() {
        return horarioSaida;
    }

    public void setHorarioSaida(String horarioSaida) {
        this.horarioSaida = horarioSaida;
    }

    public Pena getPena() {
        return pena;
    }

    public void setPena(Pena pena) {
        this.pena = pena;
    }

    public Instituicao getInstituicao() {
        return instituicao;
    }

    public void setInstituicao(Instituicao instituicao) {
        this.instituicao = instituicao;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}

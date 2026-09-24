package br.gov.sp.cpma.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "disponibilidade_instituicao")
public class DisponibilidadeInstituicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_disponibilidade")
    private Long idDisponibilidade;

    @Column(name = "dia_semana")
    private String diaSemana;

    @Column(name = "hora_inicio_1")
    private String horaInicio1;

    @Column(name = "hora_fim_1")
    private String horaFim1;

    @Column(name = "hora_inicio_2")
    private String horaInicio2;

    @Column(name = "hora_fim_2")
    private String horaFim2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_instituicao_id_instituicao", nullable = false)
    @JsonIgnore
    private Instituicao instituicao;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    public void prePersist() {
        if (this.criadoEm == null) {
            this.criadoEm = LocalDateTime.now();
        }
    }

    public Long getIdDisponibilidade() {
        return idDisponibilidade;
    }

    public void setIdDisponibilidade(Long idDisponibilidade) {
        this.idDisponibilidade = idDisponibilidade;
    }

    public String getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(String diaSemana) {
        this.diaSemana = diaSemana;
    }

    public String getHoraInicio1() {
        return horaInicio1;
    }

    public void setHoraInicio1(String horaInicio1) {
        this.horaInicio1 = horaInicio1;
    }

    public String getHoraFim1() {
        return horaFim1;
    }

    public void setHoraFim1(String horaFim1) {
        this.horaFim1 = horaFim1;
    }

    public String getHoraInicio2() {
        return horaInicio2;
    }

    public void setHoraInicio2(String horaInicio2) {
        this.horaInicio2 = horaInicio2;
    }

    public String getHoraFim2() {
        return horaFim2;
    }

    public void setHoraFim2(String horaFim2) {
        this.horaFim2 = horaFim2;
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

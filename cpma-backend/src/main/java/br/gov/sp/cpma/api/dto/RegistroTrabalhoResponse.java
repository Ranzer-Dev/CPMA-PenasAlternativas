package br.gov.sp.cpma.api.dto;

import br.gov.sp.cpma.domain.entity.RegistroDeTrabalho;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class RegistroTrabalhoResponse {

    private Long idRegistro;
    private Long penaId;
    private Long instituicaoId;
    private String instituicaoNome;
    private LocalDate dataTrabalho;
    private Double horasCumpridas;
    private String atividades;
    private String horarioInicio;
    private String horarioAlmoco;
    private String horarioVolta;
    private String horarioSaida;
    private LocalDateTime criadoEm;

    public RegistroTrabalhoResponse() {}

    public RegistroTrabalhoResponse(RegistroDeTrabalho r) {
        this.idRegistro = r.getIdRegistro();
        if (r.getPena() != null) {
            this.penaId = r.getPena().getIdPena();
        }
        if (r.getInstituicao() != null) {
            this.instituicaoId = r.getInstituicao().getIdInstituicao();
            this.instituicaoNome = r.getInstituicao().getNome();
        }
        this.dataTrabalho = r.getDataTrabalho();
        this.horasCumpridas = r.getHorasCumpridas();
        this.atividades = r.getAtividades();
        this.horarioInicio = r.getHorarioInicio();
        this.horarioAlmoco = r.getHorarioAlmoco();
        this.horarioVolta = r.getHorarioVolta();
        this.horarioSaida = r.getHorarioSaida();
        this.criadoEm = r.getCriadoEm();
    }

    public Long getIdRegistro() { return idRegistro; }
    public void setIdRegistro(Long idRegistro) { this.idRegistro = idRegistro; }
    public Long getPenaId() { return penaId; }
    public void setPenaId(Long penaId) { this.penaId = penaId; }
    public Long getInstituicaoId() { return instituicaoId; }
    public void setInstituicaoId(Long instituicaoId) { this.instituicaoId = instituicaoId; }
    public String getInstituicaoNome() { return instituicaoNome; }
    public void setInstituicaoNome(String instituicaoNome) { this.instituicaoNome = instituicaoNome; }
    public LocalDate getDataTrabalho() { return dataTrabalho; }
    public void setDataTrabalho(LocalDate dataTrabalho) { this.dataTrabalho = dataTrabalho; }
    public Double getHorasCumpridas() { return horasCumpridas; }
    public void setHorasCumpridas(Double horasCumpridas) { this.horasCumpridas = horasCumpridas; }
    public String getAtividades() { return atividades; }
    public void setAtividades(String atividades) { this.atividades = atividades; }
    public String getHorarioInicio() { return horarioInicio; }
    public void setHorarioInicio(String horarioInicio) { this.horarioInicio = horarioInicio; }
    public String getHorarioAlmoco() { return horarioAlmoco; }
    public void setHorarioAlmoco(String horarioAlmoco) { this.horarioAlmoco = horarioAlmoco; }
    public String getHorarioVolta() { return horarioVolta; }
    public void setHorarioVolta(String horarioVolta) { this.horarioVolta = horarioVolta; }
    public String getHorarioSaida() { return horarioSaida; }
    public void setHorarioSaida(String horarioSaida) { this.horarioSaida = horarioSaida; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}

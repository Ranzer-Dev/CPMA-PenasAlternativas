package br.gov.sp.cpma.domain.service;

import br.gov.sp.cpma.api.dto.CadastroRegistroTrabalhoRequest;
import br.gov.sp.cpma.api.dto.RegistroTrabalhoResponse;
import br.gov.sp.cpma.api.dto.ResumoCumprimentoResponse;
import br.gov.sp.cpma.api.exception.DomainException;
import br.gov.sp.cpma.domain.entity.Instituicao;
import br.gov.sp.cpma.domain.entity.Pena;
import br.gov.sp.cpma.domain.entity.RegistroDeTrabalho;
import br.gov.sp.cpma.domain.repository.InstituicaoRepository;
import br.gov.sp.cpma.domain.repository.PenaRepository;
import br.gov.sp.cpma.domain.repository.RegistroDeTrabalhoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

@Service
public class RegistroTrabalhoService {

    private final RegistroDeTrabalhoRepository registroRepository;
    private final PenaRepository penaRepository;
    private final InstituicaoRepository instituicaoRepository;

    public RegistroTrabalhoService(RegistroDeTrabalhoRepository registroRepository,
                                  PenaRepository penaRepository,
                                  InstituicaoRepository instituicaoRepository) {
        this.registroRepository = registroRepository;
        this.penaRepository = penaRepository;
        this.instituicaoRepository = instituicaoRepository;
    }

    @Transactional
    public RegistroTrabalhoResponse registrar(CadastroRegistroTrabalhoRequest req) {
        Pena pena = penaRepository.findById(req.getPenaId())
                .orElseThrow(() -> new DomainException("PENA_NOT_FOUND", "Pena nao encontrada", HttpStatus.NOT_FOUND));

        Instituicao instituicao = null;
        if (req.getInstituicaoId() != null) {
            instituicao = instituicaoRepository.findById(req.getInstituicaoId())
                    .orElse(pena.getInstituicaoPrincipal());
        } else {
            instituicao = pena.getInstituicaoPrincipal();
        }

        Double horas = req.getHorasCumpridas();
        if (horas == null || horas <= 0.0) {
            horas = calcularHorasPelosHorarios(req.getHorarioInicio(), req.getHorarioAlmoco(), req.getHorarioVolta(), req.getHorarioSaida());
        }

        RegistroDeTrabalho reg = new RegistroDeTrabalho();
        reg.setPena(pena);
        reg.setInstituicao(instituicao);
        reg.setDataTrabalho(req.getDataTrabalho());
        reg.setHorasCumpridas(horas);
        reg.setAtividades(req.getAtividades());
        reg.setHorarioInicio(req.getHorarioInicio());
        reg.setHorarioAlmoco(req.getHorarioAlmoco());
        reg.setHorarioVolta(req.getHorarioVolta());
        reg.setHorarioSaida(req.getHorarioSaida());

        RegistroDeTrabalho salvo = registroRepository.save(reg);
        return new RegistroTrabalhoResponse(salvo);
    }

    @Transactional(readOnly = true)
    public List<RegistroTrabalhoResponse> listarPorPena(Long penaId) {
        if (!penaRepository.existsById(penaId)) {
            throw new DomainException("PENA_NOT_FOUND", "Pena nao encontrada", HttpStatus.NOT_FOUND);
        }
        return registroRepository.findByPena_IdPenaOrderByDataTrabalhoDesc(penaId)
                .stream()
                .map(RegistroTrabalhoResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResumoCumprimentoResponse obterResumo(Long penaId) {
        Pena pena = penaRepository.findById(penaId)
                .orElseThrow(() -> new DomainException("PENA_NOT_FOUND", "Pena nao encontrada", HttpStatus.NOT_FOUND));

        Double horasCumpridas = registroRepository.somarHorasCumpridasPorPena(penaId);
        Long usuarioId = pena.getUsuario() != null ? pena.getUsuario().getIdUsuario() : null;
        String usuarioNome = pena.getUsuario() != null ? pena.getUsuario().getNome() : "";

        return new ResumoCumprimentoResponse(pena.getIdPena(), usuarioId, usuarioNome, pena.getHorasTotais(), horasCumpridas);
    }

    private Double calcularHorasPelosHorarios(String inicio, String almoco, String volta, String saida) {
        try {
            double total = 0.0;
            if (inicio != null && !inicio.isBlank() && almoco != null && !almoco.isBlank()) {
                LocalTime t1 = LocalTime.parse(inicio.trim());
                LocalTime t2 = LocalTime.parse(almoco.trim());
                total += Duration.between(t1, t2).toMinutes() / 60.0;
            }
            if (volta != null && !volta.isBlank() && saida != null && !saida.isBlank()) {
                LocalTime t3 = LocalTime.parse(volta.trim());
                LocalTime t4 = LocalTime.parse(saida.trim());
                total += Duration.between(t3, t4).toMinutes() / 60.0;
            }
            if (total == 0.0 && inicio != null && !inicio.isBlank() && saida != null && !saida.isBlank()) {
                LocalTime t1 = LocalTime.parse(inicio.trim());
                LocalTime t4 = LocalTime.parse(saida.trim());
                total = Duration.between(t1, t4).toMinutes() / 60.0;
            }
            return Math.max(0.0, total);
        } catch (Exception ex) {
            return 0.0;
        }
    }
}

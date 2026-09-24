package br.gov.sp.cpma.domain.service;

import br.gov.sp.cpma.api.dto.CadastroPenaRequest;
import br.gov.sp.cpma.api.dto.EstimativaPenaRequest;
import br.gov.sp.cpma.api.dto.EstimativaPenaResponse;
import br.gov.sp.cpma.api.dto.PenaResponse;
import br.gov.sp.cpma.api.exception.DomainException;
import br.gov.sp.cpma.domain.entity.Instituicao;
import br.gov.sp.cpma.domain.entity.Pena;
import br.gov.sp.cpma.domain.entity.Usuario;
import br.gov.sp.cpma.domain.repository.InstituicaoRepository;
import br.gov.sp.cpma.domain.repository.PenaRepository;
import br.gov.sp.cpma.domain.repository.UsuarioRepository;
import br.gov.sp.cpma.domain.util.CodigoPenaCalculator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PenaService {

    private final PenaRepository penaRepository;
    private final UsuarioRepository usuarioRepository;
    private final InstituicaoRepository instituicaoRepository;

    public PenaService(PenaRepository penaRepository,
                       UsuarioRepository usuarioRepository,
                       InstituicaoRepository instituicaoRepository) {
        this.penaRepository = penaRepository;
        this.usuarioRepository = usuarioRepository;
        this.instituicaoRepository = instituicaoRepository;
    }

    @Transactional
    public PenaResponse cadastrar(CadastroPenaRequest req) {
        Usuario usuario = usuarioRepository.findById(req.getUsuarioId())
                .orElseThrow(() -> new DomainException("USUARIO_NOT_FOUND", "Apenado nao encontrado", HttpStatus.NOT_FOUND));

        Instituicao instPrincipal = instituicaoRepository.findById(req.getInstituicaoId())
                .orElseThrow(() -> new DomainException("INSTITUICAO_NOT_FOUND", "Instituicao principal nao encontrada", HttpStatus.NOT_FOUND));

        int totalPenasAtuais = penaRepository.countByUsuario_IdUsuario(usuario.getIdUsuario());
        String novoCodigo = CodigoPenaCalculator.calcularProximoCodigo(totalPenasAtuais);

        double tempoEstimado = req.getHorasTotais() / (req.getHorasSemanais() * 4.0);

        LocalDate dataTerminoCalculada = req.getDataTermino();
        if (dataTerminoCalculada == null && req.getDataInicio() != null) {
            int dias = (int) Math.ceil((req.getHorasTotais() / (double) req.getHorasSemanais()) * 7.0);
            dataTerminoCalculada = req.getDataInicio().plusDays(dias);
        }

        Pena pena = new Pena();
        pena.setUsuario(usuario);
        pena.setInstituicaoPrincipal(instPrincipal);
        pena.setTipoPena(req.getTipoPena());
        pena.setDataInicio(req.getDataInicio());
        pena.setDataTermino(dataTerminoCalculada);
        pena.setDescricao(req.getDescricao());
        pena.setDiasSemanaEHorariosDisponivel(req.getDiasSemanaEHorariosDisponivel());
        pena.setAtividadesAcordadas(req.getAtividadesAcordadas());
        pena.setHorasSemanais(req.getHorasSemanais());
        pena.setHorasTotais(req.getHorasTotais());
        pena.setTempoPena(tempoEstimado);

        if (req.getOutrasInstituicoesIds() != null && !req.getOutrasInstituicoesIds().isEmpty()) {
            List<Instituicao> outras = instituicaoRepository.findAllById(req.getOutrasInstituicoesIds());
            pena.setInstituicoesVinculadas(new ArrayList<>(outras));
        }

        Pena salva = penaRepository.save(pena);

        usuario.setCodigo(novoCodigo);
        usuarioRepository.save(usuario);

        return new PenaResponse(salva);
    }

    @Transactional(readOnly = true)
    public List<PenaResponse> listarPorUsuario(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new DomainException("USUARIO_NOT_FOUND", "Apenado nao encontrado", HttpStatus.NOT_FOUND);
        }
        return penaRepository.findByUsuario_IdUsuarioOrderByDataInicioDesc(usuarioId)
                .stream()
                .map(PenaResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public PenaResponse buscarPorId(Long idPena) {
        Pena p = penaRepository.findById(idPena)
                .orElseThrow(() -> new DomainException("PENA_NOT_FOUND", "Pena nao encontrada", HttpStatus.NOT_FOUND));
        return new PenaResponse(p);
    }

    public EstimativaPenaResponse calcularEstimativa(EstimativaPenaRequest req) {
        double meses = req.getHorasTotais() / (req.getHorasSemanais() * 4.0);
        int dias = (int) Math.ceil((req.getHorasTotais() / (double) req.getHorasSemanais()) * 7.0);
        LocalDate termino = req.getDataInicio().plusDays(dias);
        return new EstimativaPenaResponse(meses, termino);
    }
}

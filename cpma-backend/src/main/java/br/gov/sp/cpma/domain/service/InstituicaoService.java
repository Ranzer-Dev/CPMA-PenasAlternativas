package br.gov.sp.cpma.domain.service;

import br.gov.sp.cpma.api.dto.*;
import br.gov.sp.cpma.api.exception.DomainException;
import br.gov.sp.cpma.domain.entity.DisponibilidadeInstituicao;
import br.gov.sp.cpma.domain.entity.Instituicao;
import br.gov.sp.cpma.domain.entity.TipoInstituicao;
import br.gov.sp.cpma.domain.repository.DisponibilidadeInstituicaoRepository;
import br.gov.sp.cpma.domain.repository.InstituicaoRepository;
import br.gov.sp.cpma.domain.repository.TipoInstituicaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class InstituicaoService {

    private final InstituicaoRepository instituicaoRepository;
    private final TipoInstituicaoRepository tipoInstituicaoRepository;
    private final DisponibilidadeInstituicaoRepository disponibilidadeRepository;

    public InstituicaoService(InstituicaoRepository instituicaoRepository,
                              TipoInstituicaoRepository tipoInstituicaoRepository,
                              DisponibilidadeInstituicaoRepository disponibilidadeRepository) {
        this.instituicaoRepository = instituicaoRepository;
        this.tipoInstituicaoRepository = tipoInstituicaoRepository;
        this.disponibilidadeRepository = disponibilidadeRepository;
    }

    @Transactional
    public InstituicaoResponse cadastrar(CadastroInstituicaoRequest req) {
        TipoInstituicao tipo = tipoInstituicaoRepository.findById(req.getTipoId())
                .orElseThrow(() -> new DomainException("TIPO_INSTITUICAO_NOT_FOUND", "Tipo de instituicao nao encontrado", HttpStatus.NOT_FOUND));

        Instituicao inst = new Instituicao();
        inst.setNome(req.getNome());
        inst.setEndereco(req.getEndereco());
        inst.setCidade(req.getCidade());
        inst.setUf(req.getUf());
        inst.setBairro(req.getBairro());
        inst.setCep(req.getCep());
        inst.setResponsavel(req.getResponsavel());
        inst.setTelefone(req.getTelefone());
        inst.setTipo(tipo);

        Instituicao salva = instituicaoRepository.save(inst);

        List<DisponibilidadeDTO> disponibilidadesRes = new ArrayList<>();
        if (req.getDisponibilidades() != null && !req.getDisponibilidades().isEmpty()) {
            for (DisponibilidadeDTO dDto : req.getDisponibilidades()) {
                DisponibilidadeInstituicao disp = new DisponibilidadeInstituicao();
                disp.setInstituicao(salva);
                disp.setDiaSemana(dDto.getDiaSemana());
                disp.setHoraInicio1(dDto.getHoraInicio1());
                disp.setHoraFim1(dDto.getHoraFim1());
                disp.setHoraInicio2(dDto.getHoraInicio2());
                disp.setHoraFim2(dDto.getHoraFim2());
                disponibilidadeRepository.save(disp);
                disponibilidadesRes.add(dDto);
            }
        }

        InstituicaoResponse res = new InstituicaoResponse(salva);
        res.setDisponibilidades(disponibilidadesRes);
        return res;
    }

    @Transactional
    public InstituicaoResponse atualizar(Long id, CadastroInstituicaoRequest req) {
        Instituicao inst = instituicaoRepository.findById(id)
                .orElseThrow(() -> new DomainException("INSTITUICAO_NOT_FOUND", "Instituicao nao encontrada", HttpStatus.NOT_FOUND));

        TipoInstituicao tipo = tipoInstituicaoRepository.findById(req.getTipoId())
                .orElseThrow(() -> new DomainException("TIPO_INSTITUICAO_NOT_FOUND", "Tipo de instituicao nao encontrado", HttpStatus.NOT_FOUND));

        inst.setNome(req.getNome());
        inst.setEndereco(req.getEndereco());
        inst.setCidade(req.getCidade());
        inst.setUf(req.getUf());
        inst.setBairro(req.getBairro());
        inst.setCep(req.getCep());
        inst.setResponsavel(req.getResponsavel());
        inst.setTelefone(req.getTelefone());
        inst.setTipo(tipo);

        Instituicao salva = instituicaoRepository.save(inst);

        disponibilidadeRepository.deleteByInstituicao_IdInstituicao(salva.getIdInstituicao());
        List<DisponibilidadeDTO> disponibilidadesRes = new ArrayList<>();
        if (req.getDisponibilidades() != null && !req.getDisponibilidades().isEmpty()) {
            for (DisponibilidadeDTO dDto : req.getDisponibilidades()) {
                DisponibilidadeInstituicao disp = new DisponibilidadeInstituicao();
                disp.setInstituicao(salva);
                disp.setDiaSemana(dDto.getDiaSemana());
                disp.setHoraInicio1(dDto.getHoraInicio1());
                disp.setHoraFim1(dDto.getHoraFim1());
                disp.setHoraInicio2(dDto.getHoraInicio2());
                disp.setHoraFim2(dDto.getHoraFim2());
                disponibilidadeRepository.save(disp);
                disponibilidadesRes.add(dDto);
            }
        }

        InstituicaoResponse res = new InstituicaoResponse(salva);
        res.setDisponibilidades(disponibilidadesRes);
        return res;
    }

    @Transactional(readOnly = true)
    public List<InstituicaoResponse> listarTodos() {
        List<Instituicao> instituicoes = instituicaoRepository.findByOrderByNomeAsc();
        List<InstituicaoResponse> res = new ArrayList<>();
        for (Instituicao inst : instituicoes) {
            InstituicaoResponse item = new InstituicaoResponse(inst);
            List<DisponibilidadeInstituicao> disps = disponibilidadeRepository.findByInstituicao_IdInstituicao(inst.getIdInstituicao());
            List<DisponibilidadeDTO> dDtos = new ArrayList<>();
            for (DisponibilidadeInstituicao d : disps) {
                dDtos.add(new DisponibilidadeDTO(d.getDiaSemana(), d.getHoraInicio1(), d.getHoraFim1(), d.getHoraInicio2(), d.getHoraFim2()));
            }
            item.setDisponibilidades(dDtos);
            res.add(item);
        }
        return res;
    }

    @Transactional(readOnly = true)
    public InstituicaoResponse buscarPorId(Long id) {
        Instituicao inst = instituicaoRepository.findById(id)
                .orElseThrow(() -> new DomainException("INSTITUICAO_NOT_FOUND", "Instituicao nao encontrada", HttpStatus.NOT_FOUND));
        InstituicaoResponse res = new InstituicaoResponse(inst);
        List<DisponibilidadeInstituicao> disps = disponibilidadeRepository.findByInstituicao_IdInstituicao(inst.getIdInstituicao());
        List<DisponibilidadeDTO> dDtos = new ArrayList<>();
        for (DisponibilidadeInstituicao d : disps) {
            dDtos.add(new DisponibilidadeDTO(d.getDiaSemana(), d.getHoraInicio1(), d.getHoraFim1(), d.getHoraInicio2(), d.getHoraFim2()));
        }
        res.setDisponibilidades(dDtos);
        return res;
    }

    @Transactional
    public TipoInstituicaoResponse cadastrarTipo(CadastroTipoInstituicaoRequest req) {
        TipoInstituicao tipo = new TipoInstituicao();
        tipo.setTipo(req.getTipo());
        TipoInstituicao salvo = tipoInstituicaoRepository.save(tipo);
        return new TipoInstituicaoResponse(salvo);
    }

    @Transactional(readOnly = true)
    public List<TipoInstituicaoResponse> listarTipos() {
        return tipoInstituicaoRepository.findAll().stream().map(TipoInstituicaoResponse::new).toList();
    }
}

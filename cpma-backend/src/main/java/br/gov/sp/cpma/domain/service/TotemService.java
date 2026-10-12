package br.gov.sp.cpma.domain.service;

import br.gov.sp.cpma.api.dto.*;
import br.gov.sp.cpma.api.exception.DomainException;
import br.gov.sp.cpma.domain.entity.Administrador;
import br.gov.sp.cpma.domain.entity.CodigoAcessoApenado;
import br.gov.sp.cpma.domain.entity.LogAcessoApenado;
import br.gov.sp.cpma.domain.entity.Usuario;
import br.gov.sp.cpma.domain.repository.AdministradorRepository;
import br.gov.sp.cpma.domain.repository.CodigoAcessoApenadoRepository;
import br.gov.sp.cpma.domain.repository.LogAcessoApenadoRepository;
import br.gov.sp.cpma.domain.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TotemService {

    private final TokenService tokenService;
    private final BiometriaClientService biometriaClientService;
    private final AdministradorRepository administradorRepository;
    private final UsuarioRepository usuarioRepository;
    private final CodigoAcessoApenadoRepository codigoAcessoRepository;
    private final LogAcessoApenadoRepository logAcessoRepository;
    private final SecureRandom random = new SecureRandom();

    public TotemService(
            TokenService tokenService,
            BiometriaClientService biometriaClientService,
            AdministradorRepository administradorRepository,
            UsuarioRepository usuarioRepository,
            CodigoAcessoApenadoRepository codigoAcessoRepository,
            LogAcessoApenadoRepository logAcessoRepository) {
        this.tokenService = tokenService;
        this.biometriaClientService = biometriaClientService;
        this.administradorRepository = administradorRepository;
        this.usuarioRepository = usuarioRepository;
        this.codigoAcessoRepository = codigoAcessoRepository;
        this.logAcessoRepository = logAcessoRepository;
    }

    @Transactional
    public GerarTokenTotemResponse gerarTokenTotem(GerarTokenTotemRequest request) {
        Administrador admin = administradorRepository.findById(request.getAdminId())
                .orElseThrow(() -> new DomainException("ADMIN_NOT_FOUND", "Administrador nao encontrado.", HttpStatus.NOT_FOUND));

        String jwt = tokenService.gerarTokenTotem(request.getTerminalId(), admin.getIdAdmin());
        return new GerarTokenTotemResponse(request.getTerminalId(), jwt);
    }

    @Transactional
    public CodigoAcessoResponse gerarCodigoAcesso(GerarCodigoAcessoRequest request) {
        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "Apenado nao encontrado.", HttpStatus.NOT_FOUND));

        Administrador admin = administradorRepository.findById(request.getAdminId())
                .orElseThrow(() -> new DomainException("ADMIN_NOT_FOUND", "Administrador nao encontrado.", HttpStatus.NOT_FOUND));

        codigoAcessoRepository.findFirstByUsuarioIdUsuarioAndStatusOrderByDataGeracaoDesc(usuario.getIdUsuario(), "ATIVO")
                .ifPresent(c -> {
                    c.setStatus("CANCELADO");
                    codigoAcessoRepository.save(c);
                });

        String codigo = String.format("%06d", random.nextInt(1000000));
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiracao = now.plusMinutes(request.getMinutosValidade());

        CodigoAcessoApenado novoCodigo = new CodigoAcessoApenado();
        novoCodigo.setUsuario(usuario);
        novoCodigo.setAdministrador(admin);
        novoCodigo.setCodigo(codigo);
        novoCodigo.setDataGeracao(now);
        novoCodigo.setDataExpiracao(expiracao);
        novoCodigo.setStatus("ATIVO");

        CodigoAcessoApenado salvo = codigoAcessoRepository.save(novoCodigo);

        return new CodigoAcessoResponse(
                salvo.getCodigo(),
                usuario.getIdUsuario(),
                usuario.getNome(),
                salvo.getDataGeracao(),
                salvo.getDataExpiracao(),
                salvo.getStatus()
        );
    }

    @Transactional
    public ValidarAcessoResponse validarCodigoAcesso(ValidarCodigoAcessoRequest request) {
        CodigoAcessoApenado codigoAcesso = codigoAcessoRepository
                .findByCodigoAndStatusWithLock(request.getCodigo(), "ATIVO")
                .orElse(null);

        if (codigoAcesso == null || codigoAcesso.getDataExpiracao().isBefore(LocalDateTime.now())) {
            LogAcessoApenado logFalha = new LogAcessoApenado();
            logFalha.setMetodo("CODIGO_ACESSO");
            logFalha.setSucesso(0);
            logFalha.setCodigoTentado(request.getCodigo());
            logFalha.setMensagem(codigoAcesso == null ? "Codigo nao encontrado ou inativo" : "Codigo expirado");
            logAcessoRepository.save(logFalha);

            throw new DomainException(
                    "INVALID_OR_EXPIRED_CODE",
                    "O codigo de acesso informado e invalido ou expirou.",
                    HttpStatus.BAD_REQUEST
            );
        }

        codigoAcesso.setStatus("USADO");
        codigoAcesso.setDataUso(LocalDateTime.now());
        codigoAcessoRepository.save(codigoAcesso);

        Usuario usuario = codigoAcesso.getUsuario();

        LogAcessoApenado logSucesso = new LogAcessoApenado();
        logSucesso.setUsuario(usuario);
        logSucesso.setCodigoAcesso(codigoAcesso);
        logSucesso.setMetodo("CODIGO_ACESSO");
        logSucesso.setSucesso(1);
        logSucesso.setCodigoTentado(request.getCodigo());
        logSucesso.setMensagem("Acesso validado com sucesso via codigo numerico no totem");
        logAcessoRepository.save(logSucesso);

        String tokenSessao = tokenService.gerarTokenSessaoApenado(usuario.getIdUsuario(), usuario.getNome(), usuario.getCodigo());
        return new ValidarAcessoResponse(
                true,
                "Acesso concedido com sucesso!",
                usuario.getIdUsuario(),
                usuario.getNome(),
                usuario.getCodigo(),
                tokenSessao
        );
    }

    @Transactional
    public ValidarCodigoEtapa1Response validarCodigoEtapa1(ValidarCodigoEtapa1Request request) {
        CodigoAcessoApenado codigoAcesso = codigoAcessoRepository
                .findByCodigoAndStatus(request.getCodigo(), "ATIVO")
                .orElse(null);

        if (codigoAcesso == null || codigoAcesso.getDataExpiracao().isBefore(LocalDateTime.now())) {
            LogAcessoApenado logFalha = new LogAcessoApenado();
            logFalha.setMetodo("2FA_ETAPA1_CODIGO");
            logFalha.setSucesso(0);
            logFalha.setCodigoTentado(request.getCodigo());
            logFalha.setMensagem(codigoAcesso == null ? "Codigo nao encontrado ou inativo" : "Codigo expirado");
            logAcessoRepository.save(logFalha);

            throw new DomainException(
                    "INVALID_OR_EXPIRED_CODE",
                    "O codigo de acesso informado e invalido ou expirou.",
                    HttpStatus.BAD_REQUEST
            );
        }

        Usuario usuario = codigoAcesso.getUsuario();
        if (usuario.getFoto() == null || usuario.getFoto().isBlank()) {
            throw new DomainException(
                    "USER_PHOTO_NOT_REGISTERED",
                    "O apenado titular do codigo (" + usuario.getNome() + ") nao possui foto cadastrada no sistema. Compareca ao balcao de atendimento.",
                    HttpStatus.BAD_REQUEST
            );
        }

        return new ValidarCodigoEtapa1Response(
                true,
                "Codigo validado com sucesso. Prossiga para a avaliacao biometrica facial.",
                usuario.getIdUsuario(),
                usuario.getNome(),
                usuario.getCodigo(),
                codigoAcesso.getCodigo(),
                true
        );
    }

    @Transactional
    public ValidarAcessoResponse validarBiometriaEtapa2(ValidarBiometriaEtapa2Request request) {
        CodigoAcessoApenado codigoAcesso = codigoAcessoRepository
                .findByCodigoAndStatusWithLock(request.getCodigo(), "ATIVO")
                .orElse(null);

        if (codigoAcesso == null || codigoAcesso.getDataExpiracao().isBefore(LocalDateTime.now())) {
            LogAcessoApenado logFalha = new LogAcessoApenado();
            logFalha.setMetodo("2FA_ETAPA2_BIOMETRIA");
            logFalha.setSucesso(0);
            logFalha.setCodigoTentado(request.getCodigo());
            logFalha.setMensagem(codigoAcesso == null ? "Codigo nao encontrado ou inativo na 2a etapa" : "Codigo expirado na 2a etapa");
            logAcessoRepository.save(logFalha);

            throw new DomainException(
                    "INVALID_OR_EXPIRED_CODE",
                    "O codigo de acesso informado e invalido ou expirou.",
                    HttpStatus.BAD_REQUEST
            );
        }

        Usuario usuario = codigoAcesso.getUsuario();
        if (usuario.getFoto() == null || usuario.getFoto().isBlank()) {
            throw new DomainException(
                    "USER_PHOTO_NOT_REGISTERED",
                    "O apenado titular do codigo (" + usuario.getNome() + ") nao possui foto cadastrada no sistema.",
                    HttpStatus.BAD_REQUEST
            );
        }

        BiometriaClientService.BiometriaCompareResult result = biometriaClientService.compararFaces(
                request.getFotoCapturadaBase64(),
                usuario.getFoto(),
                request.getThreshold()
        );

        if (!result.isMatch()) {
            LogAcessoApenado logFalha = new LogAcessoApenado();
            logFalha.setUsuario(usuario);
            logFalha.setCodigoAcesso(codigoAcesso);
            logFalha.setMetodo("2FA_ETAPA2_BIOMETRIA");
            logFalha.setSucesso(0);
            logFalha.setCodigoTentado(request.getCodigo());
            logFalha.setMensagem("Falha biometrica: rosto nao confere com titular " + usuario.getNome() + " (confianca: " + result.getConfidence() + ")");
            logAcessoRepository.save(logFalha);

            throw new DomainException(
                    "BIOMETRIA_MISMATCH",
                    "Reconhecimento facial nao corresponde ao apenado titular do codigo (" + usuario.getNome() + "). Tente novamente ou dirija-se ao balcao.",
                    HttpStatus.FORBIDDEN
            );
        }

        codigoAcesso.setStatus("USADO");
        codigoAcesso.setDataUso(LocalDateTime.now());
        codigoAcessoRepository.save(codigoAcesso);

        LogAcessoApenado logSucesso = new LogAcessoApenado();
        logSucesso.setUsuario(usuario);
        logSucesso.setCodigoAcesso(codigoAcesso);
        logSucesso.setMetodo("2FA_ETAPA2_BIOMETRIA");
        logSucesso.setSucesso(1);
        logSucesso.setCodigoTentado(request.getCodigo());
        logSucesso.setMensagem("Acesso 2FA validado com sucesso (Codigo + Biometria 1:1, confianca: " + result.getConfidence() + ")");
        logAcessoRepository.save(logSucesso);

        String tokenSessao = tokenService.gerarTokenSessaoApenado(usuario.getIdUsuario(), usuario.getNome(), usuario.getCodigo());
        return new ValidarAcessoResponse(
                true,
                "Autenticacao em duas etapas concluida com sucesso!",
                usuario.getIdUsuario(),
                usuario.getNome(),
                usuario.getCodigo(),
                tokenSessao
        );
    }

    @Transactional
    public ReconhecimentoFacialResponse processarReconhecimentoFacial(ReconhecimentoFacialRequest request, String tokenTotem) {
        if (tokenTotem != null && !tokenTotem.isBlank()) {
            tokenService.validarTokenTotem(tokenTotem);
        }

        List<Usuario> usuariosComFoto = usuarioRepository.findAll().stream()
                .filter(u -> u.getFoto() != null && !u.getFoto().isBlank())
                .toList();

        for (Usuario u : usuariosComFoto) {
            BiometriaClientService.BiometriaCompareResult result = biometriaClientService.compararFaces(
                    request.getFotoCapturadaBase64(),
                    u.getFoto(),
                    request.getThreshold()
            );

            if (result.isMatch()) {
                LogAcessoApenado log = new LogAcessoApenado();
                log.setUsuario(u);
                log.setMetodo("RECONHECIMENTO_FACIAL");
                log.setSucesso(1);
                log.setMensagem("Reconhecimento biometrico confirmado com confianca " + result.getConfidence());
                logAcessoRepository.save(log);

                return new ReconhecimentoFacialResponse(
                        true,
                        "Apenado reconhecido com sucesso!",
                        u.getIdUsuario(),
                        u.getNome(),
                        u.getCodigo(),
                        result.getConfidence()
                );
            }
        }

        LogAcessoApenado logFalha = new LogAcessoApenado();
        logFalha.setMetodo("RECONHECIMENTO_FACIAL");
        logFalha.setSucesso(0);
        logFalha.setMensagem("Rosto nao corresponde a nenhum apenado cadastrado");
        logAcessoRepository.save(logFalha);

        return ReconhecimentoFacialResponse.falha("Rosto nao reconhecido na base de apenados cadastrados.");
    }
}

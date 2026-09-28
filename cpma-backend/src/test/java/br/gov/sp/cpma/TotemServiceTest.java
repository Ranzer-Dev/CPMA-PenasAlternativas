package br.gov.sp.cpma;

import br.gov.sp.cpma.api.dto.*;
import br.gov.sp.cpma.api.exception.DomainException;
import br.gov.sp.cpma.domain.entity.Administrador;
import br.gov.sp.cpma.domain.entity.CodigoAcessoApenado;
import br.gov.sp.cpma.domain.entity.Usuario;
import br.gov.sp.cpma.domain.repository.AdministradorRepository;
import br.gov.sp.cpma.domain.repository.CodigoAcessoApenadoRepository;
import br.gov.sp.cpma.domain.repository.LogAcessoApenadoRepository;
import br.gov.sp.cpma.domain.repository.UsuarioRepository;
import br.gov.sp.cpma.domain.service.BiometriaClientService;
import br.gov.sp.cpma.domain.service.TokenService;
import br.gov.sp.cpma.domain.service.TotemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TotemServiceTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private BiometriaClientService biometriaClientService;

    @Mock
    private AdministradorRepository administradorRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CodigoAcessoApenadoRepository codigoAcessoRepository;

    @Mock
    private LogAcessoApenadoRepository logAcessoRepository;

    @InjectMocks
    private TotemService totemService;

    private Administrador admin;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        admin = new Administrador();
        admin.setIdAdmin(1L);
        admin.setNome("Admin Teste");

        usuario = new Usuario();
        usuario.setIdUsuario(10L);
        usuario.setNome("Apenado Silva");
        usuario.setCpf("12345678900");
        usuario.setCodigo("APN-2026-001");
        usuario.setFoto("base64_foto_cadastrada");
    }

    @Test
    @DisplayName("Deve gerar token totem com sucesso")
    void deveGerarTokenTotemComSucesso() {
        GerarTokenTotemRequest request = new GerarTokenTotemRequest();
        request.setTerminalId("TOTEM_01");
        request.setAdminId(1L);

        when(administradorRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(tokenService.gerarTokenTotem("TOTEM_01", 1L)).thenReturn("jwt.token.valido");

        GerarTokenTotemResponse response = totemService.gerarTokenTotem(request);

        assertNotNull(response);
        assertEquals("TOTEM_01", response.getTerminalId());
        assertEquals("jwt.token.valido", response.getToken());
        assertEquals("Bearer", response.getTokenType());
    }

    @Test
    @DisplayName("Deve lancar DomainException quando administrador nao for encontrado ao gerar token")
    void deveLancarExcecaoQuandoAdminNaoEncontradoAoGerarToken() {
        GerarTokenTotemRequest request = new GerarTokenTotemRequest();
        request.setTerminalId("TOTEM_01");
        request.setAdminId(99L);

        when(administradorRepository.findById(99L)).thenReturn(Optional.empty());

        DomainException ex = assertThrows(DomainException.class, () -> totemService.gerarTokenTotem(request));
        assertEquals("ADMIN_NOT_FOUND", ex.getCode());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    @DisplayName("Deve gerar codigo de acesso cancelando codigo ativo anterior")
    void deveGerarCodigoAcessoComSucesso() {
        GerarCodigoAcessoRequest request = new GerarCodigoAcessoRequest();
        request.setUsuarioId(10L);
        request.setAdminId(1L);
        request.setMinutosValidade(60);

        CodigoAcessoApenado codigoAnterior = new CodigoAcessoApenado();
        codigoAnterior.setStatus("ATIVO");

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(administradorRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(codigoAcessoRepository.findFirstByUsuarioIdUsuarioAndStatusOrderByDataGeracaoDesc(10L, "ATIVO"))
                .thenReturn(Optional.of(codigoAnterior));

        when(codigoAcessoRepository.save(any(CodigoAcessoApenado.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CodigoAcessoResponse response = totemService.gerarCodigoAcesso(request);

        assertNotNull(response);
        assertEquals(6, response.getCodigo().length());
        assertEquals("Apenado Silva", response.getUsuarioNome());
        assertEquals("ATIVO", response.getStatus());
        assertEquals("CANCELADO", codigoAnterior.getStatus());
    }

    @Test
    @DisplayName("Deve validar codigo de acesso ativo com sucesso")
    void deveValidarCodigoAcessoComSucesso() {
        ValidarCodigoAcessoRequest request = new ValidarCodigoAcessoRequest();
        request.setCodigo("123456");

        CodigoAcessoApenado codigo = new CodigoAcessoApenado();
        codigo.setCodigo("123456");
        codigo.setStatus("ATIVO");
        codigo.setDataExpiracao(LocalDateTime.now().plusHours(2));
        codigo.setUsuario(usuario);

        when(codigoAcessoRepository.findByCodigoAndStatusWithLock("123456", "ATIVO"))
                .thenReturn(Optional.of(codigo));

        ValidarAcessoResponse response = totemService.validarCodigoAcesso(request);

        assertTrue(response.isSucesso());
        assertEquals("Apenado Silva", response.getUsuarioNome());
        assertEquals("USADO", codigo.getStatus());
        verify(logAcessoRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Deve rejeitar codigo expirado e registrar log de falha")
    void deveRejeitarCodigoExpirado() {
        ValidarCodigoAcessoRequest request = new ValidarCodigoAcessoRequest();
        request.setCodigo("654321");

        CodigoAcessoApenado codigoExpirado = new CodigoAcessoApenado();
        codigoExpirado.setCodigo("654321");
        codigoExpirado.setStatus("ATIVO");
        codigoExpirado.setDataExpiracao(LocalDateTime.now().minusMinutes(5));
        codigoExpirado.setUsuario(usuario);

        when(codigoAcessoRepository.findByCodigoAndStatusWithLock("654321", "ATIVO"))
                .thenReturn(Optional.of(codigoExpirado));

        DomainException ex = assertThrows(DomainException.class, () -> totemService.validarCodigoAcesso(request));
        assertEquals("INVALID_OR_EXPIRED_CODE", ex.getCode());
        verify(logAcessoRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Deve processar reconhecimento facial com sucesso quando houver match")
    void deveProcessarReconhecimentoFacialComSucesso() {
        ReconhecimentoFacialRequest request = new ReconhecimentoFacialRequest();
        request.setFotoCapturadaBase64("base64_capturada");
        request.setThreshold(0.65);

        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
        when(biometriaClientService.compararFaces("base64_capturada", "base64_foto_cadastrada", 0.65))
                .thenReturn(new BiometriaClientService.BiometriaCompareResult(true, 0.92));

        ReconhecimentoFacialResponse response = totemService.processarReconhecimentoFacial(request, "Bearer token");

        assertTrue(response.isSucesso());
        assertEquals("Apenado Silva", response.getUsuarioNome());
        assertEquals(0.92, response.getConfidence());
        verify(logAcessoRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Deve retornar falha quando nenhum apenado corresponder a foto")
    void deveRetornarFalhaQuandoNenhumRostoCorresponder() {
        ReconhecimentoFacialRequest request = new ReconhecimentoFacialRequest();
        request.setFotoCapturadaBase64("base64_desconhecido");
        request.setThreshold(0.65);

        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
        when(biometriaClientService.compararFaces(eq("base64_desconhecido"), any(), eq(0.65)))
                .thenReturn(new BiometriaClientService.BiometriaCompareResult(false, 0.30));

        ReconhecimentoFacialResponse response = totemService.processarReconhecimentoFacial(request, "Bearer token");

        assertFalse(response.isSucesso());
        verify(logAcessoRepository, times(1)).save(any());
    }
}

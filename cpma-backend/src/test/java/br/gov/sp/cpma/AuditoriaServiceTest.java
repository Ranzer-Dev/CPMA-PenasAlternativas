package br.gov.sp.cpma;

import br.gov.sp.cpma.api.dto.EstatisticasAuditoriaDTO;
import br.gov.sp.cpma.api.dto.LogAuditoriaDTO;
import br.gov.sp.cpma.domain.entity.LogAcessoApenado;
import br.gov.sp.cpma.domain.entity.Usuario;
import br.gov.sp.cpma.domain.repository.LogAcessoApenadoRepository;
import br.gov.sp.cpma.domain.service.AuditoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

    @Mock
    private LogAcessoApenadoRepository logAcessoRepository;

    @InjectMocks
    private AuditoriaService auditoriaService;

    private Usuario usuario;
    private LogAcessoApenado logSucesso;
    private LogAcessoApenado logFalhaBiometrica;
    private LogAcessoApenado logFalhaCodigo;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setIdUsuario(10L);
        usuario.setNome("Joao Silva");
        usuario.setCpf("12345678901");
        usuario.setCodigo("PEN-100");

        logSucesso = new LogAcessoApenado();
        logSucesso.setIdLog(1L);
        logSucesso.setUsuario(usuario);
        logSucesso.setMetodo("2FA_ETAPA2_BIOMETRIA");
        logSucesso.setSucesso(1);
        logSucesso.setCodigoTentado("123456");
        logSucesso.setMensagem("Acesso 2FA validado com sucesso");
        logSucesso.setDataHora(LocalDateTime.now().minusMinutes(10));

        logFalhaBiometrica = new LogAcessoApenado();
        logFalhaBiometrica.setIdLog(2L);
        logFalhaBiometrica.setUsuario(usuario);
        logFalhaBiometrica.setMetodo("2FA_ETAPA2_BIOMETRIA");
        logFalhaBiometrica.setSucesso(0);
        logFalhaBiometrica.setCodigoTentado("123456");
        logFalhaBiometrica.setMensagem("Falha biometrica: rosto nao confere com titular");
        logFalhaBiometrica.setDataHora(LocalDateTime.now().minusMinutes(5));

        logFalhaCodigo = new LogAcessoApenado();
        logFalhaCodigo.setIdLog(3L);
        logFalhaCodigo.setMetodo("CODIGO_ACESSO");
        logFalhaCodigo.setSucesso(0);
        logFalhaCodigo.setCodigoTentado("999999");
        logFalhaCodigo.setMensagem("Codigo nao encontrado ou inativo");
        logFalhaCodigo.setDataHora(LocalDateTime.now().minusMinutes(1));
    }

    @Test
    @DisplayName("Deve listar todos os logs e classificar riscos corretamente")
    void deveListarTodosOsLogsComClassificacaoDeRisco() {
        when(logAcessoRepository.findAllByOrderByDataHoraDesc())
                .thenReturn(List.of(logFalhaCodigo, logFalhaBiometrica, logSucesso));

        List<LogAuditoriaDTO> dtos = auditoriaService.listarLogs(10, null, false);

        assertEquals(3, dtos.size());
        assertEquals("ALERTA", dtos.get(0).getNivelRisco());
        assertEquals("CRITICO", dtos.get(1).getNivelRisco());
        assertEquals("NORMAL", dtos.get(2).getNivelRisco());
        assertTrue(dtos.get(2).isSucesso());
        assertFalse(dtos.get(1).isSucesso());
    }

    @Test
    @DisplayName("Deve filtrar logs por termo de busca")
    void deveFiltrarLogsPorTermo() {
        when(logAcessoRepository.findAllByOrderByDataHoraDesc())
                .thenReturn(List.of(logFalhaCodigo, logFalhaBiometrica, logSucesso));

        List<LogAuditoriaDTO> dtos = auditoriaService.listarLogs(10, "Joao", false);

        assertEquals(2, dtos.size());
        assertEquals("Joao Silva", dtos.get(0).getUsuarioNome());
    }

    @Test
    @DisplayName("Deve calcular estatisticas de auditoria corretamente")
    void deveCalcularEstatisticasDeAuditoria() {
        when(logAcessoRepository.findAll())
                .thenReturn(List.of(logFalhaCodigo, logFalhaBiometrica, logSucesso));

        EstatisticasAuditoriaDTO stats = auditoriaService.obterEstatisticas();

        assertEquals(3, stats.getTotalLogs());
        assertEquals(1, stats.getTotalSucessos());
        assertEquals(2, stats.getTotalFalhas());
        assertEquals(1, stats.getFalhasBiometricas());
        assertEquals(1, stats.getFalhasCodigo());
    }
}

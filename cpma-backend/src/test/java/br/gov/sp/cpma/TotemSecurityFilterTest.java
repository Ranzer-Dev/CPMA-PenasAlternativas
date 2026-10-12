package br.gov.sp.cpma;

import br.gov.sp.cpma.api.controller.TotemController;
import br.gov.sp.cpma.api.dto.ValidarCodigoEtapa1Request;
import br.gov.sp.cpma.api.dto.ValidarCodigoEtapa1Response;
import br.gov.sp.cpma.api.security.TotemSecurityFilter;
import br.gov.sp.cpma.domain.repository.LogAcessoApenadoRepository;
import br.gov.sp.cpma.domain.service.TotemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TotemController.class)
@Import(TotemSecurityFilter.class)
public class TotemSecurityFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TotemService totemService;

    @MockBean
    private LogAcessoApenadoRepository logAcessoRepository;

    private static final String CHAVE_OFICIAL = "CPMA-TOTEM-KIOSK-TERMINAL-01-SP-GOV-AUTH-2026";

    @Test
    @DisplayName("Deve permitir acesso quando Chave de Terminal Oficial e IP Local forem fornecidos")
    void devePermitirAcessoComChaveEIpValidos() throws Exception {
        ValidarCodigoEtapa1Request req = new ValidarCodigoEtapa1Request();
        req.setCodigo("123456");

        ValidarCodigoEtapa1Response resp = new ValidarCodigoEtapa1Response(
                true, "Código validado com sucesso", 1L, "Carlos Silva", "APN-001", "123456", true
        );
        when(totemService.validarCodigoEtapa1(any())).thenReturn(resp);

        mockMvc.perform(post("/totem/etapa1/validar-codigo")
                        .header("X-Totem-Terminal-Key", CHAVE_OFICIAL)
                        .with(request -> {
                            request.setRemoteAddr("127.0.0.1");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.usuarioNome").value("Carlos Silva"));
    }

    @Test
    @DisplayName("Deve bloquear com 403 Forbidden quando Chave de Terminal estiver ausente ou incorreta")
    void deveBloquearQuandoChaveTerminalAusente() throws Exception {
        ValidarCodigoEtapa1Request req = new ValidarCodigoEtapa1Request();
        req.setCodigo("123456");

        mockMvc.perform(post("/totem/etapa1/validar-codigo")
                        .with(request -> {
                            request.setRemoteAddr("127.0.0.1");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED_TERMINAL_DEVICE"));
    }

    @Test
    @DisplayName("Deve bloquear com 403 Forbidden quando IP for de rede externa ou Wi-Fi nao homologado")
    void deveBloquearQuandoIpNaoAutorizado() throws Exception {
        ValidarCodigoEtapa1Request req = new ValidarCodigoEtapa1Request();
        req.setCodigo("123456");

        mockMvc.perform(post("/totem/etapa1/validar-codigo")
                        .header("X-Totem-Terminal-Key", CHAVE_OFICIAL)
                        .with(request -> {
                            request.setRemoteAddr("192.168.1.155");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED_NETWORK_IP"));
    }

    @Test
    @DisplayName("Deve permitir GET /totem/status sem necessidade de chave de terminal para monitoramento de liveness")
    void devePermitirStatusSemChave() throws Exception {
        mockMvc.perform(get("/totem/status")
                        .with(request -> {
                            request.setRemoteAddr("127.0.0.1");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}

package br.gov.sp.cpma;

import br.gov.sp.cpma.api.controller.TotemController;
import br.gov.sp.cpma.api.dto.*;
import br.gov.sp.cpma.api.exception.DomainException;
import br.gov.sp.cpma.api.exception.GlobalExceptionHandler;
import br.gov.sp.cpma.domain.service.TotemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TotemController.class)
@Import(GlobalExceptionHandler.class)
public class TotemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TotemService totemService;

    @Test
    @DisplayName("Deve gerar token JWT para terminal totem com sucesso retornando 201")
    void deveGerarTokenTotemComSucesso() throws Exception {
        GerarTokenTotemRequest request = new GerarTokenTotemRequest();
        request.setTerminalId("TOTEM_HALL_ENTRADA");
        request.setAdminId(1L);

        GerarTokenTotemResponse response = new GerarTokenTotemResponse("TOTEM_HALL_ENTRADA", "mocked.jwt.token");
        when(totemService.gerarTokenTotem(any(GerarTokenTotemRequest.class))).thenReturn(response);

        mockMvc.perform(post("/totem/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.terminalId").value("TOTEM_HALL_ENTRADA"))
                .andExpect(jsonPath("$.token").value("mocked.jwt.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("Deve gerar codigo temporario para apenado retornando 201")
    void deveGerarCodigoTemporarioComSucesso() throws Exception {
        GerarCodigoAcessoRequest request = new GerarCodigoAcessoRequest();
        request.setUsuarioId(10L);
        request.setAdminId(1L);
        request.setMinutosValidade(30);

        CodigoAcessoResponse response = new CodigoAcessoResponse(
                "847392",
                10L,
                "Jose Pereira",
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(30),
                "ATIVO"
        );
        when(totemService.gerarCodigoAcesso(any(GerarCodigoAcessoRequest.class))).thenReturn(response);

        mockMvc.perform(post("/totem/codigo-temporario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("847392"))
                .andExpect(jsonPath("$.usuarioId").value(10L))
                .andExpect(jsonPath("$.usuarioNome").value("Jose Pereira"))
                .andExpect(jsonPath("$.status").value("ATIVO"));
    }

    @Test
    @DisplayName("Deve validar codigo correto no totem retornando 200 e dados do apenado")
    void deveValidarCodigoAcessoComSucesso() throws Exception {
        ValidarCodigoAcessoRequest request = new ValidarCodigoAcessoRequest();
        request.setCodigo("847392");
        request.setTerminalId("TOTEM_ENTRADA");

        ValidarAcessoResponse response = new ValidarAcessoResponse(
                true,
                "Acesso concedido com sucesso!",
                10L,
                "Jose Pereira",
                "APN-010"
        );
        when(totemService.validarCodigoAcesso(any(ValidarCodigoAcessoRequest.class))).thenReturn(response);

        mockMvc.perform(post("/totem/validar-codigo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.usuarioNome").value("Jose Pereira"))
                .andExpect(jsonPath("$.usuarioCodigo").value("APN-010"));
    }

    @Test
    @DisplayName("Deve rejeitar codigo invalido ou expirado retornando 400 com codigo semantico")
    void deveRejeitarCodigoInvalido() throws Exception {
        ValidarCodigoAcessoRequest request = new ValidarCodigoAcessoRequest();
        request.setCodigo("000000");

        when(totemService.validarCodigoAcesso(any(ValidarCodigoAcessoRequest.class)))
                .thenThrow(new DomainException("INVALID_OR_EXPIRED_CODE", "O codigo de acesso informado e invalido ou expirou.", HttpStatus.BAD_REQUEST));

        mockMvc.perform(post("/totem/validar-codigo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_OR_EXPIRED_CODE"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Deve autenticar e reconhecer face com sucesso retornando 200")
    void deveReconhecerFaceComSucesso() throws Exception {
        ReconhecimentoFacialRequest request = new ReconhecimentoFacialRequest();
        request.setFotoCapturadaBase64("base64_capturada_valida");
        request.setThreshold(0.75);

        ReconhecimentoFacialResponse response = new ReconhecimentoFacialResponse(
                true,
                "Apenado reconhecido com sucesso!",
                10L,
                "Jose Pereira",
                "APN-010",
                0.89
        );

        when(totemService.processarReconhecimentoFacial(any(ReconhecimentoFacialRequest.class), eq("Bearer valid_token")))
                .thenReturn(response);

        mockMvc.perform(post("/totem/reconhecer")
                        .header("Authorization", "Bearer valid_token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.usuarioNome").value("Jose Pereira"))
                .andExpect(jsonPath("$.confidence").value(0.89));
    }
}

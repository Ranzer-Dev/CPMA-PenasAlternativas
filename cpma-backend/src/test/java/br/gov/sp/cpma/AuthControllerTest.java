package br.gov.sp.cpma;

import br.gov.sp.cpma.api.controller.AuthController;
import br.gov.sp.cpma.api.dto.LoginRequest;
import br.gov.sp.cpma.api.dto.LoginResponse;
import br.gov.sp.cpma.api.dto.RedefinirSenhaRequest;
import br.gov.sp.cpma.api.exception.DomainException;
import br.gov.sp.cpma.api.exception.GlobalExceptionHandler;
import br.gov.sp.cpma.domain.service.AuthService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("Deve autenticar administrador com credenciais validas retornando status 200")
    void deveAutenticarComSucesso() throws Exception {
        LoginRequest request = new LoginRequest("12345678900", "admin123");
        LoginResponse response = new LoginResponse(1L, "Administrador", "12345678900", 1, "mocked-jwt-token");

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.adminId").value(1))
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"))
                .andExpect(jsonPath("$.nome").value("Administrador"));
    }

    @Test
    @DisplayName("Deve retornar status 401 quando credenciais forem incorretas")
    void deveRetornar401QuandoCredenciaisIncorretas() throws Exception {
        LoginRequest request = new LoginRequest("12345678900", "senhaErrada");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new DomainException("INVALID_CREDENTIALS", "CPF ou senha incorretos.", HttpStatus.UNAUTHORIZED));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("Deve rejeitar requisicao com 422 quando CPF ou senha forem em branco")
    void deveRejeitarCamposEmBranco() throws Exception {
        LoginRequest request = new LoginRequest("", "");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("Deve redefinir senha com sucesso retornando status 200")
    void deveRedefinirSenha() throws Exception {
        RedefinirSenhaRequest request = new RedefinirSenhaRequest("12345678900", "Belem", "novaSenha123");
        doNothing().when(authService).redefinirSenha(any(RedefinirSenhaRequest.class));

        mockMvc.perform(post("/auth/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Senha redefinida com sucesso."));
    }
}

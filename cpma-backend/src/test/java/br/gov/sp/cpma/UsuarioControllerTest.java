package br.gov.sp.cpma;

import br.gov.sp.cpma.api.controller.UsuarioController;
import br.gov.sp.cpma.api.dto.CadastroUsuarioRequest;
import br.gov.sp.cpma.api.dto.UsuarioResponse;
import br.gov.sp.cpma.api.exception.DomainException;
import br.gov.sp.cpma.api.exception.GlobalExceptionHandler;
import br.gov.sp.cpma.domain.entity.Usuario;
import br.gov.sp.cpma.domain.service.UsuarioService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@Import(GlobalExceptionHandler.class)
public class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioService usuarioService;

    @Test
    @DisplayName("Deve cadastrar apenado com sucesso retornando status 201")
    void deveCadastrarApenadoComSucesso() throws Exception {
        CadastroUsuarioRequest request = new CadastroUsuarioRequest();
        request.setNome("Carlos Alberto da Silva");
        request.setCpf("12345678901");
        request.setCodigo("APN-001");
        request.setAdminId(1L);

        Usuario entity = new Usuario();
        entity.setIdUsuario(1L);
        entity.setNome(request.getNome());
        entity.setCpf(request.getCpf());
        entity.setCodigo(request.getCodigo());

        UsuarioResponse response = new UsuarioResponse(entity);
        when(usuarioService.cadastrar(any(CadastroUsuarioRequest.class))).thenReturn(response);

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idUsuario").value(1L))
                .andExpect(jsonPath("$.nome").value("Carlos Alberto da Silva"))
                .andExpect(jsonPath("$.cpf").value("12345678901"))
                .andExpect(jsonPath("$.codigo").value("APN-001"));
    }

    @Test
    @DisplayName("Deve rejeitar requisicao com campos obrigatorios vazios retornando 422 e detalhes dos campos")
    void deveRejeitarCamposObrigatoriosVazios() throws Exception {
        CadastroUsuarioRequest requestInvalido = new CadastroUsuarioRequest();

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("Deve retornar conflito 409 quando o CPF ja estiver cadastrado")
    void deveRetornarConflitoCpfDuplicado() throws Exception {
        CadastroUsuarioRequest request = new CadastroUsuarioRequest();
        request.setNome("Carlos Alberto da Silva");
        request.setCpf("12345678901");
        request.setCodigo("APN-001");
        request.setAdminId(1L);

        when(usuarioService.cadastrar(any(CadastroUsuarioRequest.class)))
                .thenThrow(new DomainException("CPF_ALREADY_EXISTS", "Ja existe um apenado cadastrado com este CPF.", HttpStatus.CONFLICT));

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CPF_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("Deve retornar 404 quando usuario nao for encontrado por ID")
    void deveRetornar404UsuarioNaoEncontrado() throws Exception {
        when(usuarioService.buscarPorId(999L))
                .thenThrow(new DomainException("USER_NOT_FOUND", "Apenado nao encontrado", HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/usuarios/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
    }
}

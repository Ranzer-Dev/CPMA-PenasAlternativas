package br.gov.sp.cpma;

import br.gov.sp.cpma.api.controller.InstituicaoController;
import br.gov.sp.cpma.api.dto.CadastroInstituicaoRequest;
import br.gov.sp.cpma.api.dto.InstituicaoResponse;
import br.gov.sp.cpma.api.exception.GlobalExceptionHandler;
import br.gov.sp.cpma.domain.entity.Instituicao;
import br.gov.sp.cpma.domain.entity.TipoInstituicao;
import br.gov.sp.cpma.domain.service.InstituicaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InstituicaoController.class)
@Import(GlobalExceptionHandler.class)
public class InstituicaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InstituicaoService instituicaoService;

    @Test
    @DisplayName("Deve cadastrar instituicao com sucesso retornando status 201")
    void deveCadastrarInstituicaoComSucesso() throws Exception {
        CadastroInstituicaoRequest request = new CadastroInstituicaoRequest();
        request.setNome("Lar dos Idosos Sao Vicente");
        request.setTipoId(1L);
        request.setCidade("Belem");
        request.setUf("PA");

        Instituicao entity = new Instituicao();
        entity.setIdInstituicao(10L);
        entity.setNome(request.getNome());
        entity.setCidade(request.getCidade());
        entity.setUf(request.getUf());
        TipoInstituicao tipo = new TipoInstituicao();
        tipo.setIdTipo(1L);
        tipo.setTipo("Filantropica");
        entity.setTipo(tipo);
        entity.setCriadoEm(LocalDateTime.now());

        InstituicaoResponse response = new InstituicaoResponse(entity);
        when(instituicaoService.cadastrar(any(CadastroInstituicaoRequest.class))).thenReturn(response);

        mockMvc.perform(post("/instituicoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idInstituicao").value(10))
                .andExpect(jsonPath("$.nome").value("Lar dos Idosos Sao Vicente"));
    }

    @Test
    @DisplayName("Deve rejeitar requisicao com 422 quando nome ou tipoId faltarem")
    void deveRejeitarQuandoCamposObrigatoriosFaltarem() throws Exception {
        CadastroInstituicaoRequest request = new CadastroInstituicaoRequest();

        mockMvc.perform(post("/instituicoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("Deve listar instituicoes com sucesso retornando status 200")
    void deveListarInstituicoes() throws Exception {
        Instituicao entity = new Instituicao();
        entity.setIdInstituicao(1L);
        entity.setNome("Horta Comunitaria");
        InstituicaoResponse item = new InstituicaoResponse(entity);

        when(instituicaoService.listarTodos()).thenReturn(List.of(item));

        mockMvc.perform(get("/instituicoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Horta Comunitaria"));
    }
}

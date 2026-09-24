package br.gov.sp.cpma;

import br.gov.sp.cpma.api.controller.PenaController;
import br.gov.sp.cpma.api.dto.CadastroPenaRequest;
import br.gov.sp.cpma.api.dto.EstimativaPenaRequest;
import br.gov.sp.cpma.api.dto.EstimativaPenaResponse;
import br.gov.sp.cpma.api.dto.PenaResponse;
import br.gov.sp.cpma.api.exception.GlobalExceptionHandler;
import br.gov.sp.cpma.domain.entity.Instituicao;
import br.gov.sp.cpma.domain.entity.Pena;
import br.gov.sp.cpma.domain.entity.Usuario;
import br.gov.sp.cpma.domain.service.PenaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PenaController.class)
@Import(GlobalExceptionHandler.class)
public class PenaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PenaService penaService;

    @Test
    @DisplayName("Deve cadastrar pena com sucesso retornando status 201")
    void deveCadastrarPenaComSucesso() throws Exception {
        CadastroPenaRequest request = new CadastroPenaRequest();
        request.setUsuarioId(1L);
        request.setInstituicaoId(2L);
        request.setTipoPena("Prestacao de Servicos a Comunidade");
        request.setDataInicio(LocalDate.of(2026, 1, 10));
        request.setHorasSemanais(8);
        request.setHorasTotais(160);

        Pena pena = new Pena();
        pena.setIdPena(100L);
        pena.setTipoPena(request.getTipoPena());
        pena.setDataInicio(request.getDataInicio());
        pena.setHorasSemanais(8);
        pena.setHorasTotais(160);
        pena.setTempoPena(5.0);
        pena.setCriadoEm(LocalDateTime.now());

        Usuario u = new Usuario();
        u.setIdUsuario(1L);
        u.setNome("Carlos Silva");
        u.setCodigo("1a");
        pena.setUsuario(u);

        Instituicao inst = new Instituicao();
        inst.setIdInstituicao(2L);
        inst.setNome("Asilo Sao Jose");
        pena.setInstituicaoPrincipal(inst);

        PenaResponse response = new PenaResponse(pena);
        when(penaService.cadastrar(any(CadastroPenaRequest.class))).thenReturn(response);

        mockMvc.perform(post("/penas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPena").value(100))
                .andExpect(jsonPath("$.usuarioNome").value("Carlos Silva"))
                .andExpect(jsonPath("$.horasTotais").value(160));
    }

    @Test
    @DisplayName("Deve rejeitar requisicao com 422 quando campos de pena forem invalidos")
    void deveRejeitarQuandoCamposPenaFaltarem() throws Exception {
        CadastroPenaRequest request = new CadastroPenaRequest();

        mockMvc.perform(post("/penas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("Deve listar penas por usuario retornando status 200")
    void deveListarPenasPorUsuario() throws Exception {
        Pena pena = new Pena();
        pena.setIdPena(1L);
        pena.setTipoPena("PSC");
        pena.setDataInicio(LocalDate.now());
        pena.setHorasSemanais(10);
        pena.setHorasTotais(100);
        pena.setTempoPena(2.5);
        pena.setCriadoEm(LocalDateTime.now());

        when(penaService.listarPorUsuario(1L)).thenReturn(List.of(new PenaResponse(pena)));

        mockMvc.perform(get("/penas/usuario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idPena").value(1));
    }

    @Test
    @DisplayName("Deve calcular estimativa de pena retornando status 200")
    void deveCalcularEstimativaPena() throws Exception {
        EstimativaPenaRequest request = new EstimativaPenaRequest();
        request.setHorasTotais(160);
        request.setHorasSemanais(8);
        request.setDataInicio(LocalDate.of(2026, 1, 1));

        EstimativaPenaResponse response = new EstimativaPenaResponse(5.0, LocalDate.of(2026, 5, 22));
        when(penaService.calcularEstimativa(any(EstimativaPenaRequest.class))).thenReturn(response);

        mockMvc.perform(post("/penas/estimativa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tempoEstimadoMeses").value(5.0));
    }
}

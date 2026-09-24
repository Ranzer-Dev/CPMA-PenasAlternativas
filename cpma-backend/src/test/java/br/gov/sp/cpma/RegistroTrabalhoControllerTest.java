package br.gov.sp.cpma;

import br.gov.sp.cpma.api.controller.RegistroTrabalhoController;
import br.gov.sp.cpma.api.dto.CadastroRegistroTrabalhoRequest;
import br.gov.sp.cpma.api.dto.RegistroTrabalhoResponse;
import br.gov.sp.cpma.api.dto.ResumoCumprimentoResponse;
import br.gov.sp.cpma.api.exception.GlobalExceptionHandler;
import br.gov.sp.cpma.domain.entity.Instituicao;
import br.gov.sp.cpma.domain.entity.Pena;
import br.gov.sp.cpma.domain.entity.RegistroDeTrabalho;
import br.gov.sp.cpma.domain.service.RegistroTrabalhoService;
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

@WebMvcTest(RegistroTrabalhoController.class)
@Import(GlobalExceptionHandler.class)
public class RegistroTrabalhoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegistroTrabalhoService registroService;

    @Test
    @DisplayName("Deve registrar horas de trabalho com sucesso retornando status 201")
    void deveRegistrarHorasDeTrabalhoComSucesso() throws Exception {
        CadastroRegistroTrabalhoRequest request = new CadastroRegistroTrabalhoRequest();
        request.setPenaId(1L);
        request.setDataTrabalho(LocalDate.of(2026, 3, 1));
        request.setHorasCumpridas(4.0);
        request.setAtividades("Manutencao de jardim");

        RegistroDeTrabalho entity = new RegistroDeTrabalho();
        entity.setIdRegistro(50L);
        entity.setDataTrabalho(request.getDataTrabalho());
        entity.setHorasCumpridas(4.0);
        entity.setAtividades(request.getAtividades());
        entity.setCriadoEm(LocalDateTime.now());

        Pena pena = new Pena();
        pena.setIdPena(1L);
        entity.setPena(pena);

        Instituicao inst = new Instituicao();
        inst.setIdInstituicao(10L);
        inst.setNome("Parque Municipal");
        entity.setInstituicao(inst);

        RegistroTrabalhoResponse response = new RegistroTrabalhoResponse(entity);
        when(registroService.registrar(any(CadastroRegistroTrabalhoRequest.class))).thenReturn(response);

        mockMvc.perform(post("/registros-trabalho")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idRegistro").value(50))
                .andExpect(jsonPath("$.horasCumpridas").value(4.0))
                .andExpect(jsonPath("$.instituicaoNome").value("Parque Municipal"));
    }

    @Test
    @DisplayName("Deve retornar resumo de cumprimento com percentual e status")
    void deveRetornarResumoCumprimento() throws Exception {
        ResumoCumprimentoResponse resumo = new ResumoCumprimentoResponse(1L, 10L, "Carlos", 100, 40.0);
        when(registroService.obterResumo(1L)).thenReturn(resumo);

        mockMvc.perform(get("/registros-trabalho/pena/1/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.horasTotais").value(100))
                .andExpect(jsonPath("$.horasCumpridas").value(40.0))
                .andExpect(jsonPath("$.horasRestantes").value(60.0))
                .andExpect(jsonPath("$.percentualConcluido").value(40.0))
                .andExpect(jsonPath("$.status").value("EM_CUMPRIMENTO"));
    }

    @Test
    @DisplayName("Deve listar historico de registros de uma pena")
    void deveListarRegistrosPorPena() throws Exception {
        RegistroDeTrabalho r = new RegistroDeTrabalho();
        r.setIdRegistro(1L);
        r.setDataTrabalho(LocalDate.now());
        r.setHorasCumpridas(2.5);
        r.setCriadoEm(LocalDateTime.now());

        when(registroService.listarPorPena(1L)).thenReturn(List.of(new RegistroTrabalhoResponse(r)));

        mockMvc.perform(get("/registros-trabalho/pena/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].horasCumpridas").value(2.5));
    }
}

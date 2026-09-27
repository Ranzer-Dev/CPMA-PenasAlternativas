package br.gov.sp.cpma.desktop.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DtoSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("Deve serializar e desserializar TokenTotemDTO corretamente")
    void deveSerializarTokenTotemDTO() throws Exception {
        TokenTotemDTO original = new TokenTotemDTO("TOTEM_01", "jwt.token.exemplo", "Bearer");
        String json = objectMapper.writeValueAsString(original);

        TokenTotemDTO deserialized = objectMapper.readValue(json, TokenTotemDTO.class);
        assertEquals("TOTEM_01", deserialized.getTerminalId());
        assertEquals("jwt.token.exemplo", deserialized.getToken());
        assertEquals("Bearer", deserialized.getTokenType());
    }

    @Test
    @DisplayName("Deve serializar e desserializar ReconhecimentoFacialDTO com confianca")
    void deveSerializarReconhecimentoFacialDTO() throws Exception {
        ReconhecimentoFacialDTO original = new ReconhecimentoFacialDTO();
        original.setSucesso(true);
        original.setMensagem("Reconhecido");
        original.setUsuarioId(15L);
        original.setUsuarioNome("Carlos Silva");
        original.setUsuarioCodigo("APN-001");
        original.setConfidence(0.88);

        String json = objectMapper.writeValueAsString(original);
        ReconhecimentoFacialDTO deserialized = objectMapper.readValue(json, ReconhecimentoFacialDTO.class);

        assertTrue(deserialized.isSucesso());
        assertEquals(15L, deserialized.getUsuarioId());
        assertEquals("Carlos Silva", deserialized.getUsuarioNome());
        assertEquals(0.88, deserialized.getConfidence());
    }

    @Test
    @DisplayName("Deve serializar e desserializar ValidarAcessoDTO")
    void deveSerializarValidarAcessoDTO() throws Exception {
        ValidarAcessoDTO dto = new ValidarAcessoDTO(true, "Acesso liberado", 20L, "Mariana", "APN-020");
        String json = objectMapper.writeValueAsString(dto);

        ValidarAcessoDTO deserialized = objectMapper.readValue(json, ValidarAcessoDTO.class);
        assertTrue(deserialized.isSucesso());
        assertEquals("Mariana", deserialized.getUsuarioNome());
        assertEquals(20L, deserialized.getUsuarioId());
    }

    @Test
    @DisplayName("Deve serializar e desserializar StandardApiError")
    void deveSerializarStandardApiError() throws Exception {
        StandardApiError error = new StandardApiError();
        error.setStatus(404);
        error.setCode("NOT_FOUND");
        error.setMessage("Recurso inexistente");

        String json = objectMapper.writeValueAsString(error);
        StandardApiError deserialized = objectMapper.readValue(json, StandardApiError.class);

        assertEquals(404, deserialized.getStatus());
        assertEquals("NOT_FOUND", deserialized.getCode());
        assertEquals("Recurso inexistente", deserialized.getMessage());
    }
}

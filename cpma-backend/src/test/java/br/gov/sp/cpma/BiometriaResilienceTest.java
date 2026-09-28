package br.gov.sp.cpma;

import br.gov.sp.cpma.api.exception.DomainException;
import br.gov.sp.cpma.domain.service.BiometriaClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

public class BiometriaResilienceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Deve lancar BIOMETRIC_SERVICE_UNAVAILABLE com HTTP 503 quando microservico estiver offline")
    void deveLancarExcecaoQuandoServicoBiometricoEstiverOffline() {
        BiometriaClientService service = new BiometriaClientService(
                "http://127.0.0.1:59999",
                objectMapper
        );

        DomainException ex = assertThrows(DomainException.class, () ->
                service.compararFaces("fotoA_fake", "fotoB_fake", 0.65)
        );

        assertEquals("BIOMETRIC_SERVICE_UNAVAILABLE", ex.getCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatus());
        assertTrue(ex.getMessage().contains("servico local de biometria facial nao esta disponivel"));
    }

    @Test
    @DisplayName("Deve inicializar BiometriaCompareResult com valores validos")
    void deveCriarResultadoBiometriaCorretamente() {
        BiometriaClientService.BiometriaCompareResult resultMatch =
                new BiometriaClientService.BiometriaCompareResult(true, 0.95);
        assertTrue(resultMatch.isMatch());
        assertEquals(0.95, resultMatch.getConfidence(), 0.001);

        BiometriaClientService.BiometriaCompareResult resultMiss =
                new BiometriaClientService.BiometriaCompareResult(false, 0.20);
        assertFalse(resultMiss.isMatch());
        assertEquals(0.20, resultMiss.getConfidence(), 0.001);
    }
}

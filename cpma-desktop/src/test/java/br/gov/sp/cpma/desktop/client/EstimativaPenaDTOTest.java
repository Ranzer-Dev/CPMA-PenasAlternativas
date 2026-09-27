package br.gov.sp.cpma.desktop.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EstimativaPenaDTOTest {

    @Test
    @DisplayName("Deve instanciar e manipular DTO de estimativa de cumprimento de pena")
    void deveManipularEstimativaPenaDTO() {
        LocalDate inicio = LocalDate.of(2026, 3, 1);
        LocalDate termino = LocalDate.of(2026, 9, 1);

        EstimativaPenaDTO dto = new EstimativaPenaDTO(160, 8, inicio);
        dto.setTempoEstimadoMeses(5.0);
        dto.setDataTerminoEstimada(termino);

        assertEquals(160, dto.getHorasTotais());
        assertEquals(8, dto.getHorasSemanais());
        assertEquals(inicio, dto.getDataInicio());
        assertEquals(5.0, dto.getTempoEstimadoMeses());
        assertEquals(termino, dto.getDataTerminoEstimada());
    }

    @Test
    @DisplayName("Deve permitir construtor vazio e setters")
    void deveInstanciarComConstrutorPadrao() {
        EstimativaPenaDTO dto = new EstimativaPenaDTO();
        dto.setHorasTotais(200);
        dto.setHorasSemanais(10);

        assertNotNull(dto);
        assertEquals(200, dto.getHorasTotais());
        assertEquals(10, dto.getHorasSemanais());
    }
}

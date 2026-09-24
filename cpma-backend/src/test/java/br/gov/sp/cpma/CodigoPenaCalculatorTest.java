package br.gov.sp.cpma;

import br.gov.sp.cpma.domain.util.CodigoPenaCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CodigoPenaCalculatorTest {

    @Test
    @DisplayName("Deve calcular primeiro codigo como 1a para usuario sem penas")
    void deveCalcularPrimeiroCodigo() {
        String proximo = CodigoPenaCalculator.calcularProximoCodigo(0);
        assertEquals("1a", proximo);
    }

    @Test
    @DisplayName("Deve calcular 1b para quem tem 1 pena e 1z para quem tem 25 penas")
    void deveCalcularCodigosSequenciais() {
        assertEquals("1b", CodigoPenaCalculator.calcularProximoCodigo(1));
        assertEquals("1z", CodigoPenaCalculator.calcularProximoCodigo(25));
    }

    @Test
    @DisplayName("Deve virar o grupo para 2a na 26a pena")
    void deveVirarGrupoPara2a() {
        assertEquals("2a", CodigoPenaCalculator.calcularProximoCodigo(26));
        assertEquals("2b", CodigoPenaCalculator.calcularProximoCodigo(27));
    }

    @Test
    @DisplayName("Deve calcular codigo atual corretamente")
    void deveCalcularCodigoAtual() {
        assertEquals("Nenhuma pena cadastrada", CodigoPenaCalculator.calcularCodigoAtual(0));
        assertEquals("1a", CodigoPenaCalculator.calcularCodigoAtual(1));
        assertEquals("1z", CodigoPenaCalculator.calcularCodigoAtual(26));
    }
}

package br.gov.sp.cpma;

import br.gov.sp.cpma.domain.util.CalculadoraExecucaoPenal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

public class CalculadoraExecucaoPenalTest {

    @ParameterizedTest
    @CsvSource({
            "0, 0.0",
            "-10, 0.0",
            "15, 0.25",
            "30, 0.50",
            "45, 0.75",
            "60, 1.00",
            "90, 1.50",
            "120, 2.00",
            "135, 2.25",
            "50, 0.83"
    })
    @DisplayName("Deve converter minutos em horas decimais com precisao")
    void deveConverterMinutosParaHoras(long minutos, double horasEsperadas) {
        double resultado = CalculadoraExecucaoPenal.converterMinutosParaHoras(minutos);
        assertEquals(horasEsperadas, resultado, 0.001);
    }

    @ParameterizedTest
    @CsvSource({
            "0.0, 0",
            "-1.5, 0",
            "0.25, 15",
            "0.50, 30",
            "0.75, 45",
            "1.00, 60",
            "1.50, 90",
            "2.25, 135"
    })
    @DisplayName("Deve converter horas decimais em minutos inteiros")
    void deveConverterHorasParaMinutos(double horas, long minutosEsperados) {
        long resultado = CalculadoraExecucaoPenal.converterHorasParaMinutos(horas);
        assertEquals(minutosEsperados, resultado);
    }

    @Test
    @DisplayName("Deve calcular horas de trabalho em dois turnos com intervalo de almoco")
    void deveCalcularHorasDoisTurnosComAlmoco() {
        LocalTime inicio = LocalTime.of(8, 0);
        LocalTime almoco = LocalTime.of(12, 0);
        LocalTime volta = LocalTime.of(13, 0);
        LocalTime saida = LocalTime.of(17, 0);

        double horas = CalculadoraExecucaoPenal.calcularHorasPorTurnos(inicio, almoco, volta, saida);
        assertEquals(8.0, horas, 0.001);
    }

    @Test
    @DisplayName("Deve calcular horas com turnos fracionados de 30 e 45 minutos")
    void deveCalcularHorasTurnosFracionados() {
        LocalTime inicio = LocalTime.of(8, 30);
        LocalTime almoco = LocalTime.of(12, 0);
        LocalTime volta = LocalTime.of(13, 0);
        LocalTime saida = LocalTime.of(16, 45);

        double horas = CalculadoraExecucaoPenal.calcularHorasPorTurnos(inicio, almoco, volta, saida);
        assertEquals(7.25, horas, 0.001);
    }

    @Test
    @DisplayName("Deve calcular horas em turno continuo unico sem almoco")
    void deveCalcularHorasTurnoContinuo() {
        LocalTime inicio = LocalTime.of(8, 0);
        LocalTime saida = LocalTime.of(14, 0);

        double horas = CalculadoraExecucaoPenal.calcularHorasPorTurnos(inicio, null, null, saida);
        assertEquals(6.0, horas, 0.001);
    }

    @Test
    @DisplayName("Deve retornar zero para turnos com horarios invertidos ou nulos")
    void deveRetornarZeroParaTurnosInvalidos() {
        LocalTime inicio = LocalTime.of(17, 0);
        LocalTime saida = LocalTime.of(8, 0);

        double horas = CalculadoraExecucaoPenal.calcularHorasPorTurnos(inicio, null, null, saida);
        assertEquals(0.0, horas, 0.001);

        double horasNulas = CalculadoraExecucaoPenal.calcularHorasPorTurnos(null, null, null, null);
        assertEquals(0.0, horasNulas, 0.001);
    }

    @ParameterizedTest
    @CsvSource({
            "4.0, false",
            "7.0, false",
            "8.0, false",
            "8.01, true",
            "9.0, true",
            "12.0, true"
    })
    @DisplayName("Deve validar conformidade com limite maximo de 8 horas diarias da LEP")
    void deveValidarLimiteDiarioLep(double horasNoDia, boolean deveExceder) {
        boolean resultado = CalculadoraExecucaoPenal.excedeLimiteDiarioLep(horasNoDia);
        assertEquals(deveExceder, resultado);
    }

    @ParameterizedTest
    @CsvSource({
            "5.0, false",
            "6.9, false",
            "7.0, true",
            "8.0, true",
            "14.0, true"
    })
    @DisplayName("Deve validar conformidade com limite minimo de 7 horas semanais da LEP")
    void deveValidarMinimoSemanalLep(double horasSemanais, boolean deveAtender) {
        boolean resultado = CalculadoraExecucaoPenal.atendeMinimoSemanalLep(horasSemanais);
        assertEquals(deveAtender, resultado);
    }

    @Test
    @DisplayName("Deve calcular saldo restante sem permitir valor negativo")
    void deveCalcularSaldoRestante() {
        assertEquals(50.0, CalculadoraExecucaoPenal.calcularSaldoRestante(100.0, 50.0), 0.001);
        assertEquals(0.0, CalculadoraExecucaoPenal.calcularSaldoRestante(100.0, 100.0), 0.001);
        assertEquals(0.0, CalculadoraExecucaoPenal.calcularSaldoRestante(100.0, 120.0), 0.001);
        assertEquals(0.0, CalculadoraExecucaoPenal.calcularSaldoRestante(0.0, 10.0), 0.001);
        assertEquals(100.0, CalculadoraExecucaoPenal.calcularSaldoRestante(100.0, -5.0), 0.001);
    }

    @Test
    @DisplayName("Deve calcular percentual cumprido com arredondamento preciso")
    void deveCalcularPercentualCumprido() {
        assertEquals(0.0, CalculadoraExecucaoPenal.calcularPercentualCumprido(100.0, 0.0), 0.001);
        assertEquals(50.0, CalculadoraExecucaoPenal.calcularPercentualCumprido(100.0, 50.0), 0.001);
        assertEquals(33.33, CalculadoraExecucaoPenal.calcularPercentualCumprido(150.0, 50.0), 0.001);
        assertEquals(100.0, CalculadoraExecucaoPenal.calcularPercentualCumprido(100.0, 100.0), 0.001);
        assertEquals(100.0, CalculadoraExecucaoPenal.calcularPercentualCumprido(100.0, 110.0), 0.001);
        assertEquals(0.0, CalculadoraExecucaoPenal.calcularPercentualCumprido(0.0, 50.0), 0.001);
    }

    @Test
    @DisplayName("Deve calcular projecao de termino passando corretamente por ano bissexto")
    void deveCalcularProjecaoPassandoPorAnoBissexto() {
        LocalDate dataInicio = LocalDate.of(2024, 1, 15);
        int horasTotais = 140;
        int horasSemanais = 14;

        int dias = CalculadoraExecucaoPenal.calcularDiasNecessarios(horasTotais, horasSemanais);
        assertEquals(70, dias);

        LocalDate termino = CalculadoraExecucaoPenal.calcularDataTerminoEstimada(dataInicio, horasTotais, horasSemanais);
        assertEquals(LocalDate.of(2024, 3, 25), termino);
        assertTrue(dataInicio.isBefore(LocalDate.of(2024, 2, 29)));
        assertTrue(termino.isAfter(LocalDate.of(2024, 2, 29)));
    }

    @Test
    @DisplayName("Deve calcular projecao de termino em ano nao bissexto")
    void deveCalcularProjecaoAnoNaoBissexto() {
        LocalDate dataInicio = LocalDate.of(2025, 1, 15);
        int horasTotais = 140;
        int horasSemanais = 14;

        LocalDate termino = CalculadoraExecucaoPenal.calcularDataTerminoEstimada(dataInicio, horasTotais, horasSemanais);
        assertEquals(LocalDate.of(2025, 3, 26), termino);
    }

    @Test
    @DisplayName("Deve lancar excecao ao calcular estimativa com data nula")
    void deveLancarExcecaoComDataNula() {
        assertThrows(IllegalArgumentException.class, () ->
                CalculadoraExecucaoPenal.calcularDataTerminoEstimada(null, 100, 10));
    }

    @Test
    @DisplayName("Deve recalcular termino compensando horas de faltas justificadas ou nao")
    void deveRecalcularTerminoComFaltas() {
        LocalDate terminoOriginal = LocalDate.of(2026, 6, 1);
        int horasFaltas = 14;
        int horasSemanais = 7;

        LocalDate novoTermino = CalculadoraExecucaoPenal.recalcularTerminoComFaltas(terminoOriginal, horasFaltas, horasSemanais);
        assertEquals(LocalDate.of(2026, 6, 15), novoTermino);

        LocalDate mesmoTermino = CalculadoraExecucaoPenal.recalcularTerminoComFaltas(terminoOriginal, 0, 7);
        assertEquals(terminoOriginal, mesmoTermino);
    }

    @Test
    @DisplayName("Deve lancar excecao ao recalcular termino com data original nula")
    void deveLancarExcecaoAoRecalcularTerminoComDataNula() {
        assertThrows(IllegalArgumentException.class, () ->
                CalculadoraExecucaoPenal.recalcularTerminoComFaltas(null, 10, 7));
    }
}

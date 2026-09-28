package br.gov.sp.cpma.domain.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

public final class CalculadoraExecucaoPenal {

    private static final double LIMITE_MAXIMO_HORAS_DIARIAS_LEP = 8.0;
    private static final double LIMITE_MINIMO_HORAS_SEMANAIS_LEP = 7.0;

    private CalculadoraExecucaoPenal() {
    }

    public static double converterMinutosParaHoras(long minutos) {
        if (minutos <= 0) {
            return 0.0;
        }
        return BigDecimal.valueOf(minutos)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public static long converterHorasParaMinutos(double horas) {
        if (horas <= 0.0) {
            return 0L;
        }
        return Math.round(horas * 60.0);
    }

    public static double calcularHorasPorTurnos(LocalTime inicio, LocalTime almoco, LocalTime volta, LocalTime saida) {
        long totalMinutos = 0;

        if (inicio != null && almoco != null && almoco.isAfter(inicio)) {
            totalMinutos += Duration.between(inicio, almoco).toMinutes();
        }

        if (volta != null && saida != null && saida.isAfter(volta)) {
            totalMinutos += Duration.between(volta, saida).toMinutes();
        }

        if (totalMinutos == 0 && inicio != null && saida != null && saida.isAfter(inicio)) {
            totalMinutos = Duration.between(inicio, saida).toMinutes();
        }

        return converterMinutosParaHoras(totalMinutos);
    }

    public static double calcularSaldoRestante(double horasTotais, double horasCumpridas) {
        if (horasTotais <= 0.0) {
            return 0.0;
        }
        double saldo = horasTotais - Math.max(0.0, horasCumpridas);
        return Math.max(0.0, arredondarDuasCasas(saldo));
    }

    public static double calcularPercentualCumprido(double horasTotais, double horasCumpridas) {
        if (horasTotais <= 0.0 || horasCumpridas <= 0.0) {
            return 0.0;
        }
        if (horasCumpridas >= horasTotais) {
            return 100.0;
        }
        double percentual = (horasCumpridas / horasTotais) * 100.0;
        return arredondarDuasCasas(percentual);
    }

    public static boolean excedeLimiteDiarioLep(double horasNoDia) {
        return horasNoDia > LIMITE_MAXIMO_HORAS_DIARIAS_LEP;
    }

    public static boolean atendeMinimoSemanalLep(double horasSemanais) {
        return horasSemanais >= LIMITE_MINIMO_HORAS_SEMANAIS_LEP;
    }

    public static int calcularDiasNecessarios(int horasTotais, int horasSemanais) {
        if (horasTotais <= 0 || horasSemanais <= 0) {
            return 0;
        }
        double semanas = (double) horasTotais / (double) horasSemanais;
        return (int) Math.ceil(semanas * 7.0);
    }

    public static LocalDate calcularDataTerminoEstimada(LocalDate dataInicio, int horasTotais, int horasSemanais) {
        if (dataInicio == null) {
            throw new IllegalArgumentException("Data de inicio nao pode ser nula");
        }
        int diasNecessarios = calcularDiasNecessarios(horasTotais, horasSemanais);
        return dataInicio.plusDays(diasNecessarios);
    }

    public static LocalDate recalcularTerminoComFaltas(LocalDate terminoOriginal, int horasFaltas, int horasSemanais) {
        if (terminoOriginal == null) {
            throw new IllegalArgumentException("Data de termino original nao pode ser nula");
        }
        if (horasFaltas <= 0 || horasSemanais <= 0) {
            return terminoOriginal;
        }
        int diasCompensatorios = (int) Math.ceil(((double) horasFaltas / (double) horasSemanais) * 7.0);
        return terminoOriginal.plusDays(diasCompensatorios);
    }

    private static double arredondarDuasCasas(double valor) {
        return BigDecimal.valueOf(valor)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}

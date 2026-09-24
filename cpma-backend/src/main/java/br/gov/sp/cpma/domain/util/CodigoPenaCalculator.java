package br.gov.sp.cpma.domain.util;

public final class CodigoPenaCalculator {

    private CodigoPenaCalculator() {}

    public static String calcularProximoCodigo(int numeroPenas) {
        if (numeroPenas <= 0) {
            return "1a";
        }
        int numeroGrupo = (numeroPenas / 26) + 1;
        int posicaoLetra = (numeroPenas % 26);
        char letra = (char) ('a' + posicaoLetra);
        return numeroGrupo + String.valueOf(letra);
    }

    public static String calcularCodigoAtual(int numeroPenas) {
        if (numeroPenas <= 0) {
            return "Nenhuma pena cadastrada";
        }
        int numeroGrupo = ((numeroPenas - 1) / 26) + 1;
        int posicaoLetra = ((numeroPenas - 1) % 26);
        char letra = (char) ('a' + posicaoLetra);
        return numeroGrupo + String.valueOf(letra);
    }
}

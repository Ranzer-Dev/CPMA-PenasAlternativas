package util;

import java.security.SecureRandom;

/**
 * Utilitário para gerar códigos numéricos de acesso ao totem.
 *
 * O código é numérico, com 8 dígitos, sortado de forma criptograficamente
 * segura. A unicidade definitiva é garantida pelo DAO ao verificar contra
 * códigos ATIVOS já existentes.
 */
public final class CodigoAcessoUtil {

    public static final int TAMANHO_CODIGO = 8;
    public static final int VALIDADE_HORAS_PADRAO = 72;

    private static final SecureRandom RANDOM = new SecureRandom();

    private CodigoAcessoUtil() {
    }

    public static String gerarCodigo() {
        StringBuilder sb = new StringBuilder(TAMANHO_CODIGO);
        for (int i = 0; i < TAMANHO_CODIGO; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    public static String formatarParaExibicao(String codigo) {
        if (codigo == null) {
            return "";
        }
        String limpo = codigo.replaceAll("[^0-9]", "");
        if (limpo.length() != TAMANHO_CODIGO) {
            return codigo;
        }
        return limpo.substring(0, 4) + " " + limpo.substring(4);
    }

    public static String normalizar(String entrada) {
        if (entrada == null) {
            return "";
        }
        return entrada.replaceAll("[^0-9]", "");
    }
}

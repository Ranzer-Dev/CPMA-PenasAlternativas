package util;

/**
 * Utilitário para gerenciar códigos de penas seguindo a regra de negócio:
 * - Formato: 1a, 1b, 1c, ... 1z, 2a, 2b, etc.
 * - O número indica a sequência de grupos de 26 penas
 * - A letra indica a posição dentro do grupo (a-z = 1-26)
 */
public class CodigoPenaUtil {

    /**
     * Calcula o código da próxima pena baseado no número total de penas já cometidas.
     * 
     * @param numeroPenas Número total de penas já cadastradas para o usuário
     * @return Código no formato (número)(letra), ex: "1a", "1z", "2a"
     */
    public static String calcularProximoCodigo(int numeroPenas) {
        // Se não tem penas ainda, começa com 1a
        if (numeroPenas == 0) {
            return "1a";
        }

        // Calcula o número do grupo (1, 2, 3, ...)
        // Se tem 26 penas, está no grupo 1 (mas a próxima será 2a)
        // Se tem 1 pena, está no grupo 1 (próxima será 1b)
        int numeroGrupo = (numeroPenas / 26) + 1;
        
        // Calcula a letra dentro do grupo (0-25 = a-z)
        // numeroPenas % 26 dá a posição atual (0-25)
        // +1 porque queremos a PRÓXIMA letra
        int posicaoLetra = (numeroPenas % 26);
        
        // Converte para letra (0=a, 1=b, ..., 25=z)
        char letra = (char) ('a' + posicaoLetra);
        
        return numeroGrupo + String.valueOf(letra);
    }

    /**
     * Calcula o código atual baseado no número de penas.
     * Diferente do próximo, este retorna o código da última pena cadastrada.
     * 
     * @param numeroPenas Número total de penas cadastradas
     * @return Código no formato (número)(letra)
     */
    public static String calcularCodigoAtual(int numeroPenas) {
        if (numeroPenas == 0) {
            return "Nenhuma pena cadastrada";
        }

        int numeroGrupo = ((numeroPenas - 1) / 26) + 1;
        int posicaoLetra = ((numeroPenas - 1) % 26);
        char letra = (char) ('a' + posicaoLetra);
        
        return numeroGrupo + String.valueOf(letra);
    }

    /**
     * Valida se um código está no formato correto.
     * 
     * @param codigo Código a ser validado
     * @return true se o formato está correto (número + letra minúscula)
     */
    public static boolean validarFormatoCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return false;
        }
        
        // Formato esperado: número seguido de letra minúscula
        // Ex: "1a", "25z", "2a"
        return codigo.matches("\\d+[a-z]");
    }
}


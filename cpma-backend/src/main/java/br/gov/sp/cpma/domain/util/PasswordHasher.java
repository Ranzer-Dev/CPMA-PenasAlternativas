package br.gov.sp.cpma.domain.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class PasswordHasher {

    private PasswordHasher() {}

    public static String hash(String rawPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao calcular hash de senha", e);
        }
    }

    public static boolean matches(String rawPassword, String expectedHash) {
        if (rawPassword == null || expectedHash == null) {
            return false;
        }
        return hash(rawPassword).equalsIgnoreCase(expectedHash.trim());
    }
}

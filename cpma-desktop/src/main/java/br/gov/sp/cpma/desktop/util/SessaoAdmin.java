package br.gov.sp.cpma.desktop.util;

import br.gov.sp.cpma.desktop.client.LoginResponseDTO;

public final class SessaoAdmin {

    private static LoginResponseDTO adminLogado;

    private SessaoAdmin() {}

    public static void setAdminLogado(LoginResponseDTO admin) {
        adminLogado = admin;
    }

    public static LoginResponseDTO getAdminLogado() {
        return adminLogado;
    }

    public static boolean isAutenticado() {
        return adminLogado != null && adminLogado.getToken() != null;
    }

    public static void logout() {
        adminLogado = null;
    }
}

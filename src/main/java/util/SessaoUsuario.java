package util;
import model.Administrador;

public class SessaoUsuario {
    private static Administrador adminLogado;

    public static void setAdminLogado(Administrador admin) {
        adminLogado = admin;
    }

    public static Administrador getAdminLogado() {
        return adminLogado;
    }

}

package br.gov.sp.cpma.desktop.util;

import br.gov.sp.cpma.desktop.client.LoginResponseDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SessaoAdminTest {

    @AfterEach
    void tearDown() {
        SessaoAdmin.logout();
    }

    @Test
    @DisplayName("Deve inicializar sessao como nao autenticada")
    void deveInicializarNaoAutenticada() {
        assertNull(SessaoAdmin.getAdminLogado());
        assertFalse(SessaoAdmin.isAutenticado());
    }

    @Test
    @DisplayName("Deve registrar admin e validar estado autenticado com token")
    void deveRegistrarAdminEAutenticar() {
        LoginResponseDTO admin = new LoginResponseDTO();
        admin.setAdminId(1L);
        admin.setNome("Administrador Chefe");
        admin.setCpf("12345678900");
        admin.setNivelPermissao(2);
        admin.setToken("token.jwt.valido");

        SessaoAdmin.setAdminLogado(admin);

        assertTrue(SessaoAdmin.isAutenticado());
        assertEquals("Administrador Chefe", SessaoAdmin.getAdminLogado().getNome());
        assertEquals(1L, SessaoAdmin.getAdminLogado().getAdminId());
    }

    @Test
    @DisplayName("Deve considerar nao autenticado quando token for nulo")
    void deveConsiderarNaoAutenticadoSemToken() {
        LoginResponseDTO admin = new LoginResponseDTO();
        admin.setAdminId(2L);
        admin.setNome("Admin Sem Token");
        admin.setToken(null);

        SessaoAdmin.setAdminLogado(admin);

        assertFalse(SessaoAdmin.isAutenticado());
    }

    @Test
    @DisplayName("Deve resetar sessao ao efetuar logout")
    void deveResetarSessaoNoLogout() {
        LoginResponseDTO admin = new LoginResponseDTO();
        admin.setToken("token123");
        SessaoAdmin.setAdminLogado(admin);
        assertTrue(SessaoAdmin.isAutenticado());

        SessaoAdmin.logout();
        assertNull(SessaoAdmin.getAdminLogado());
        assertFalse(SessaoAdmin.isAutenticado());
    }
}

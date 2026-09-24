package br.gov.sp.cpma.desktop;

import br.gov.sp.cpma.desktop.client.ApiResponse;
import br.gov.sp.cpma.desktop.client.CpmaApiClient;
import br.gov.sp.cpma.desktop.client.LoginResponseDTO;
import br.gov.sp.cpma.desktop.client.UsuarioDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CpmaApiClientTest {

    @Test
    @DisplayName("Deve inicializar cliente com URL base padrao")
    void deveInicializarComUrlPadrao() {
        CpmaApiClient client = new CpmaApiClient();
        assertNotNull(client);
    }

    @Test
    @DisplayName("Deve retornar status 503 com NETWORK_ERROR quando servidor estiver inacessivel")
    void deveRetornarErroDeRedeQuandoServidorInacessivel() {
        CpmaApiClient client = new CpmaApiClient("http://127.0.0.1:59999/api/v1");
        ApiResponse<java.util.List<UsuarioDTO>> response = client.listarUsuarios();

        assertFalse(response.isSuccess());
        assertEquals(503, response.getStatusCode());
        assertNotNull(response.getError());
        assertEquals("NETWORK_ERROR", response.getError().getCode());
    }

    @Test
    @DisplayName("Deve tratar falha de rede no login retornando 503 com NETWORK_ERROR")
    void deveTratarFalhaDeRedeNoLogin() {
        CpmaApiClient client = new CpmaApiClient("http://127.0.0.1:59999/api/v1");
        ApiResponse<LoginResponseDTO> response = client.login("12345678900", "admin123");

        assertFalse(response.isSuccess());
        assertEquals(503, response.getStatusCode());
        assertNotNull(response.getError());
        assertEquals("NETWORK_ERROR", response.getError().getCode());
    }
}

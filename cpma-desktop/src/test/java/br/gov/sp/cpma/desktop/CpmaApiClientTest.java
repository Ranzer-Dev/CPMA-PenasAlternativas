package br.gov.sp.cpma.desktop;

import br.gov.sp.cpma.desktop.client.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        ApiResponse<List<UsuarioDTO>> response = client.listarUsuarios();

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

    @Test
    @DisplayName("Deve tratar falha de rede ao gerar token de terminal totem")
    void deveTratarFalhaDeRedeAoGerarTokenTotem() {
        CpmaApiClient client = new CpmaApiClient("http://127.0.0.1:59999/api/v1");
        ApiResponse<TokenTotemDTO> response = client.gerarTokenTotem("TOTEM_EXTERNO_01", 1L);

        assertFalse(response.isSuccess());
        assertEquals(503, response.getStatusCode());
        assertNotNull(response.getError());
        assertEquals("NETWORK_ERROR", response.getError().getCode());
    }

    @Test
    @DisplayName("Deve tratar falha de rede no reconhecimento facial do totem")
    void deveTratarFalhaDeRedeNoReconhecimentoFacial() {
        CpmaApiClient client = new CpmaApiClient("http://127.0.0.1:59999/api/v1");
        ApiResponse<ReconhecimentoFacialDTO> response = client.reconhecerFacial("base64fake", "tokenfake", 0.65);

        assertFalse(response.isSuccess());
        assertEquals(503, response.getStatusCode());
        assertNotNull(response.getError());
        assertEquals("NETWORK_ERROR", response.getError().getCode());
    }

    @Test
    @DisplayName("Deve tratar falha de rede na validacao de codigo de acesso")
    void deveTratarFalhaDeRedeNaValidacaoCodigoAcesso() {
        CpmaApiClient client = new CpmaApiClient("http://127.0.0.1:59999/api/v1");
        ApiResponse<ValidarAcessoDTO> response = client.validarCodigoAcesso("123456", "TOTEM_EXTERNO_01");

        assertFalse(response.isSuccess());
        assertEquals(503, response.getStatusCode());
        assertNotNull(response.getError());
        assertEquals("NETWORK_ERROR", response.getError().getCode());
    }
}

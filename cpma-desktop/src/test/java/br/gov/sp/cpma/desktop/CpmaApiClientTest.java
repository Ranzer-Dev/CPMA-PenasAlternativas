package br.gov.sp.cpma.desktop;

import br.gov.sp.cpma.desktop.client.ApiResponse;
import br.gov.sp.cpma.desktop.client.CpmaApiClient;
import br.gov.sp.cpma.desktop.client.LoginResponseDTO;
import br.gov.sp.cpma.desktop.client.ReconhecimentoFacialDTO;
import br.gov.sp.cpma.desktop.client.TokenTotemDTO;
import br.gov.sp.cpma.desktop.client.UsuarioDTO;
import br.gov.sp.cpma.desktop.client.ValidarAcessoDTO;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CpmaApiClientTest {

    @Test
    @DisplayName("Deve inicializar cliente com URL base padrao")
    void deveInicializarComUrlPadrao() {
        CpmaApiClient client = new CpmaApiClient();
        assertNotNull(client);
    }

    @Test
    @DisplayName("Deve resolver URL base customizada via System Property cpma.api.url")
    void deveResolverUrlCustomizadaViaSystemProperty() {
        System.setProperty("cpma.api.url", "http://192.168.1.100:8080/api/v1");
        try {
            CpmaApiClient client = new CpmaApiClient();
            assertNotNull(client);
        } finally {
            System.clearProperty("cpma.api.url");
        }
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

    @Test
    @DisplayName("Deve processar resposta de sucesso 200 OK com payload JSON valido")
    void deveProcessarResposta200ComJsonValido() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/v1/usuarios", exchange -> {
            byte[] responseBytes = "[{\"idUsuario\":1,\"nome\":\"Carlos\",\"cpf\":\"12345678901\"}]".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        });
        server.start();

        try {
            int port = server.getAddress().getPort();
            CpmaApiClient client = new CpmaApiClient("http://127.0.0.1:" + port + "/api/v1");
            ApiResponse<List<UsuarioDTO>> response = client.listarUsuarios();

            assertTrue(response.isSuccess());
            assertEquals(200, response.getStatusCode());
            assertNotNull(response.getData());
            assertEquals(1, response.getData().size());
            assertEquals("Carlos", response.getData().get(0).getNome());
        } finally {
            server.stop(0);
        }
    }

    @Test
    @DisplayName("Deve converter erro estruturado 422 JSON no objeto StandardApiError")
    void deveProcessarErroEstruturado422() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/v1/auth/login", exchange -> {
            String errorJson = "{\"status\":422,\"code\":\"VALIDATION_FAILED\",\"message\":\"Credenciais invalidas\"}";
            byte[] responseBytes = errorJson.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(422, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        });
        server.start();

        try {
            int port = server.getAddress().getPort();
            CpmaApiClient client = new CpmaApiClient("http://127.0.0.1:" + port + "/api/v1");
            ApiResponse<LoginResponseDTO> response = client.login("12345678900", "senhaErrada");

            assertFalse(response.isSuccess());
            assertEquals(422, response.getStatusCode());
            assertNotNull(response.getError());
            assertEquals("VALIDATION_FAILED", response.getError().getCode());
            assertEquals("Credenciais invalidas", response.getError().getMessage());
        } finally {
            server.stop(0);
        }
    }

    @Test
    @DisplayName("Deve acionar fallback quando servidor retornar erro 500 com texto simples ou HTML")
    void deveAcionarFallbackEmErroTextoSimples500() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/v1/usuarios", exchange -> {
            byte[] responseBytes = "Internal Error: Database timeout".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(500, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        });
        server.start();

        try {
            int port = server.getAddress().getPort();
            CpmaApiClient client = new CpmaApiClient("http://127.0.0.1:" + port + "/api/v1");
            ApiResponse<List<UsuarioDTO>> response = client.listarUsuarios();

            assertFalse(response.isSuccess());
            assertEquals(500, response.getStatusCode());
            assertNotNull(response.getError());
            assertEquals("HTTP_500", response.getError().getCode());
            assertEquals("Internal Error: Database timeout", response.getError().getMessage());
        } finally {
            server.stop(0);
        }
    }

    @Test
    @DisplayName("Deve acionar fallback padrao quando servidor retornar status de erro com corpo vazio")
    void deveAcionarFallbackPadraoEmCorpoVazio() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/v1/usuarios", exchange -> {
            exchange.sendResponseHeaders(502, -1);
        });
        server.start();

        try {
            int port = server.getAddress().getPort();
            CpmaApiClient client = new CpmaApiClient("http://127.0.0.1:" + port + "/api/v1");
            ApiResponse<List<UsuarioDTO>> response = client.listarUsuarios();

            assertFalse(response.isSuccess());
            assertEquals(502, response.getStatusCode());
            assertNotNull(response.getError());
            assertEquals("HTTP_502", response.getError().getCode());
            assertEquals("Erro de comunicacao com o servidor", response.getError().getMessage());
        } finally {
            server.stop(0);
        }
    }
}

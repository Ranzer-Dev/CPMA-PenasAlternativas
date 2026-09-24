package br.gov.sp.cpma.desktop.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CpmaApiClient {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public CpmaApiClient() {
        this("http://localhost:8080/api/v1");
    }

    public CpmaApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public ApiResponse<LoginResponseDTO> login(String cpf, String senha) {
        Map<String, String> payload = new HashMap<>();
        payload.put("cpf", cpf);
        payload.put("senha", senha);
        return executePost("/auth/login", payload, LoginResponseDTO.class, null);
    }

    public ApiResponse<UsuarioDTO> cadastrarUsuario(CadastroUsuarioDTO dto) {
        return executePost("/usuarios", dto, UsuarioDTO.class, null);
    }

    public ApiResponse<List<UsuarioDTO>> listarUsuarios() {
        return executeGet("/usuarios", new TypeReference<List<UsuarioDTO>>() {});
    }

    public ApiResponse<UsuarioDTO> buscarUsuarioPorCpf(String cpf) {
        return executeGetSingle("/usuarios/cpf/" + cpf, UsuarioDTO.class);
    }

    public ApiResponse<UsuarioDTO> buscarUsuarioPorId(Long id) {
        return executeGetSingle("/usuarios/" + id, UsuarioDTO.class);
    }

    public ApiResponse<InstituicaoDTO> cadastrarInstituicao(CadastroInstituicaoDTO dto) {
        return executePost("/instituicoes", dto, InstituicaoDTO.class, null);
    }

    public ApiResponse<List<InstituicaoDTO>> listarInstituicoes() {
        return executeGet("/instituicoes", new TypeReference<List<InstituicaoDTO>>() {});
    }

    public ApiResponse<List<TipoInstituicaoDTO>> listarTiposInstituicao() {
        return executeGet("/instituicoes/tipos", new TypeReference<List<TipoInstituicaoDTO>>() {});
    }

    public ApiResponse<PenaDTO> cadastrarPena(CadastroPenaDTO dto) {
        return executePost("/penas", dto, PenaDTO.class, null);
    }

    public ApiResponse<List<PenaDTO>> listarPenasPorUsuario(Long usuarioId) {
        return executeGet("/penas/usuario/" + usuarioId, new TypeReference<List<PenaDTO>>() {});
    }

    public ApiResponse<EstimativaPenaDTO> calcularEstimativaPena(EstimativaPenaDTO dto) {
        return executePost("/penas/estimativa", dto, EstimativaPenaDTO.class, null);
    }

    public ApiResponse<RegistroTrabalhoDTO> registrarTrabalho(RegistroTrabalhoDTO dto) {
        return executePost("/registros-trabalho", dto, RegistroTrabalhoDTO.class, null);
    }

    public ApiResponse<List<RegistroTrabalhoDTO>> listarRegistrosPorPena(Long penaId) {
        return executeGet("/registros-trabalho/pena/" + penaId, new TypeReference<List<RegistroTrabalhoDTO>>() {});
    }

    public ApiResponse<ResumoCumprimentoDTO> obterResumoCumprimento(Long penaId) {
        return executeGetSingle("/registros-trabalho/pena/" + penaId + "/resumo", ResumoCumprimentoDTO.class);
    }

    public ApiResponse<ValidarAcessoDTO> validarCodigoAcesso(String codigo, String terminalId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("codigo", codigo);
        payload.put("terminalId", terminalId);
        return executePost("/totem/validar-codigo", payload, ValidarAcessoDTO.class, null);
    }

    public ApiResponse<ReconhecimentoFacialDTO> reconhecerFacial(String fotoBase64, String tokenTotem, double threshold) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("fotoCapturadaBase64", fotoBase64);
        payload.put("threshold", threshold);
        return executePost("/totem/reconhecer", payload, ReconhecimentoFacialDTO.class, tokenTotem);
    }

    
    public ApiResponse<TipoInstituicaoDTO> cadastrarTipoInstituicao(String tipo) {
        Map<String, String> payload = new HashMap<>();
        payload.put("tipo", tipo);
        return executePost("/instituicoes/tipos", payload, TipoInstituicaoDTO.class, null);
    }

    public ApiResponse<CodigoAcessoDTO> gerarCodigoAcesso(Long usuarioId, Long adminId, Integer minutosValidade) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("usuarioId", usuarioId);
        payload.put("adminId", adminId);
        payload.put("minutosValidade", minutosValidade != null ? minutosValidade : 1440);
        return executePost("/totem/codigo-temporario", payload, CodigoAcessoDTO.class, null);
    }

    private <T> ApiResponse<T> executeGetSingle(String endpoint, Class<T> responseClass) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + endpoint))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                T data = objectMapper.readValue(response.body(), responseClass);
                return new ApiResponse<>(response.statusCode(), data);
            } else {
                StandardApiError error = parseError(response.body(), response.statusCode());
                return new ApiResponse<>(response.statusCode(), error);
            }
        } catch (Exception e) {
            return new ApiResponse<>(503, createNetworkError(e));
        }
    }

    private <T> ApiResponse<T> executeGet(String endpoint, TypeReference<T> typeRef) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + endpoint))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                T data = objectMapper.readValue(response.body(), typeRef);
                return new ApiResponse<>(response.statusCode(), data);
            } else {
                StandardApiError error = parseError(response.body(), response.statusCode());
                return new ApiResponse<>(response.statusCode(), error);
            }
        } catch (Exception e) {
            return new ApiResponse<>(503, createNetworkError(e));
        }
    }

    private <T> ApiResponse<T> executePost(String endpoint, Object body, Class<T> responseClass, String bearerToken) {
        try {
            String jsonBody = objectMapper.writeValueAsString(body);

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + endpoint))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

            if (bearerToken != null && !bearerToken.isBlank()) {
                builder.header("Authorization", bearerToken.startsWith("Bearer ") ? bearerToken : "Bearer " + bearerToken);
            }

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                T data = objectMapper.readValue(response.body(), responseClass);
                return new ApiResponse<>(response.statusCode(), data);
            } else {
                StandardApiError error = parseError(response.body(), response.statusCode());
                return new ApiResponse<>(response.statusCode(), error);
            }
        } catch (Exception e) {
            return new ApiResponse<>(503, createNetworkError(e));
        }
    }

    private StandardApiError createNetworkError(Exception e) {
        StandardApiError networkError = new StandardApiError();
        networkError.setStatus(503);
        networkError.setCode("NETWORK_ERROR");
        networkError.setMessage("Nao foi possivel conectar ao servidor backend em " + baseUrl + ": " + e.getMessage());
        return networkError;
    }

    private StandardApiError parseError(String responseBody, int statusCode) {
        try {
            return objectMapper.readValue(responseBody, StandardApiError.class);
        } catch (Exception ignored) {
            StandardApiError fallback = new StandardApiError();
            fallback.setStatus(statusCode);
            fallback.setCode("HTTP_" + statusCode);
            fallback.setMessage(responseBody != null && !responseBody.isBlank() ? responseBody : "Erro de comunicacao com o servidor");
            return fallback;
        }
    }
}

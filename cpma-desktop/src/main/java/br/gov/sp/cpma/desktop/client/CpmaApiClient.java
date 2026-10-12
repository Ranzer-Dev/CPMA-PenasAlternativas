package br.gov.sp.cpma.desktop.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CpmaApiClient {

    private final String baseUrl;
    private final String facialServiceUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public CpmaApiClient() {
        this(resolveDefaultBaseUrl(), resolveDefaultFacialServiceUrl());
    }

    private static String resolveDefaultBaseUrl() {
        String prop = System.getProperty("cpma.api.url");
        if (prop != null && !prop.isBlank()) {
            return prop;
        }
        String env = System.getenv("CPMA_API_URL");
        if (env != null && !env.isBlank()) {
            return env;
        }
        return "http://localhost:8080/api/v1";
    }

    private static String resolveDefaultFacialServiceUrl() {
        String prop = System.getProperty("cpma.facial.url");
        if (prop != null && !prop.isBlank()) {
            return prop;
        }
        String env = System.getenv("CPMA_FACIAL_URL");
        if (env != null && !env.isBlank()) {
            return env;
        }
        return "http://127.0.0.1:8001";
    }

    public CpmaApiClient(String baseUrl) {
        this(baseUrl, resolveDefaultFacialServiceUrl());
    }

    public CpmaApiClient(String baseUrl, String facialServiceUrl) {
        this.baseUrl = baseUrl;
        this.facialServiceUrl = facialServiceUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public String getFacialServiceUrl() {
        return facialServiceUrl;
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

    public ApiResponse<UsuarioDTO> atualizarUsuario(Long id, CadastroUsuarioDTO dto) {
        return executePut("/usuarios/" + id, dto, UsuarioDTO.class, null);
    }

    public ApiResponse<InstituicaoDTO> cadastrarInstituicao(CadastroInstituicaoDTO dto) {
        return executePost("/instituicoes", dto, InstituicaoDTO.class, null);
    }

    public ApiResponse<InstituicaoDTO> atualizarInstituicao(Long id, CadastroInstituicaoDTO dto) {
        return executePut("/instituicoes/" + id, dto, InstituicaoDTO.class, null);
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

    public ApiResponse<PenaDTO> atualizarPena(Long idPena, CadastroPenaDTO dto) {
        return executePut("/penas/" + idPena, dto, PenaDTO.class, null);
    }

    public ApiResponse<List<PenaDTO>> listarTodasPenas() {
        return executeGet("/penas", new TypeReference<List<PenaDTO>>() {});
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

    public ApiResponse<List<RegistroTrabalhoDTO>> registrarTrabalhoEmLote(List<RegistroTrabalhoDTO> lista) {
        return executePostList("/registros-trabalho/lote", lista, new TypeReference<List<RegistroTrabalhoDTO>>() {}, null);
    }

    public ApiResponse<List<RegistroTrabalhoDTO>> listarRegistrosPorPena(Long penaId) {
        return executeGet("/registros-trabalho/pena/" + penaId, new TypeReference<List<RegistroTrabalhoDTO>>() {});
    }

    public ApiResponse<List<RegistroTrabalhoDTO>> listarRegistrosPorPenaEMes(Long penaId, int ano, int mes) {
        return executeGet("/registros-trabalho/pena/" + penaId + "/mes/" + ano + "/" + mes, new TypeReference<List<RegistroTrabalhoDTO>>() {});
    }

    public ApiResponse<RegistroTrabalhoDTO> buscarUltimoRegistroPorPena(Long penaId) {
        return executeGetSingle("/registros-trabalho/pena/" + penaId + "/ultimo", RegistroTrabalhoDTO.class);
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

    public ApiResponse<ValidarCodigoEtapa1DTO> validarCodigoEtapa1(String codigo, String terminalId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("codigo", codigo);
        payload.put("terminalId", terminalId);
        return executePost("/totem/etapa1/validar-codigo", payload, ValidarCodigoEtapa1DTO.class, null);
    }

    public ApiResponse<ValidarAcessoDTO> validarBiometriaEtapa2(String codigo, String fotoBase64, String terminalId, Double threshold) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("codigo", codigo);
        payload.put("fotoCapturadaBase64", fotoBase64);
        payload.put("terminalId", terminalId);
        payload.put("threshold", threshold != null ? threshold : 0.65);
        return executePost("/totem/etapa2/validar-biometria", payload, ValidarAcessoDTO.class, null);
    }

    public ApiResponse<ReconhecimentoFacialDTO> reconhecerFacial(String fotoBase64, String tokenTotem, double threshold) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("fotoCapturadaBase64", fotoBase64);
        payload.put("threshold", threshold);
        return executePost("/totem/reconhecer", payload, ReconhecimentoFacialDTO.class, tokenTotem);
    }

    public ApiResponse<TokenTotemDTO> gerarTokenTotem(String terminalId, Long adminId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("terminalId", terminalId);
        payload.put("adminId", adminId);
        return executePost("/totem/token", payload, TokenTotemDTO.class, null);
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

    public ApiResponse<List<LogAuditoriaDTO>> listarLogsAuditoria(Integer limite, String termo, Boolean apenasFalhas) {
        StringBuilder url = new StringBuilder("/auditoria/logs?");
        if (limite != null) {
            url.append("limite=").append(limite).append("&");
        }
        if (termo != null && !termo.isBlank()) {
            url.append("termo=").append(URLEncoder.encode(termo.trim(), StandardCharsets.UTF_8)).append("&");
        }
        if (apenasFalhas != null) {
            url.append("apenasFalhas=").append(apenasFalhas).append("&");
        }
        return executeGet(url.toString(), new TypeReference<List<LogAuditoriaDTO>>() {});
    }

    public ApiResponse<EstatisticasAuditoriaDTO> obterEstatisticasAuditoria() {
        return executeGetSingle("/auditoria/estatisticas", EstatisticasAuditoriaDTO.class);
    }

    public ApiResponse<CapturaCameraDTO> capturarFotoWebcam() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(facialServiceUrl + "/camera/capture"))
                    .timeout(Duration.ofSeconds(6))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                CapturaCameraDTO data = objectMapper.readValue(response.body(), CapturaCameraDTO.class);
                return new ApiResponse<>(response.statusCode(), data);
            } else {
                StandardApiError error = parseError(response.body(), response.statusCode());
                return new ApiResponse<>(response.statusCode(), error);
            }
        } catch (Exception e) {
            return new ApiResponse<>(503, createNetworkError(e));
        }
    }

    private String resolveBearerToken(String explicitToken) {
        if (explicitToken != null && !explicitToken.isBlank()) {
            return explicitToken;
        }
        if (br.gov.sp.cpma.desktop.util.SessaoAdmin.isAutenticado()) {
            return br.gov.sp.cpma.desktop.util.SessaoAdmin.getAdminLogado().getToken();
        }
        return null;
    }

    private <T> ApiResponse<T> executeGetSingle(String endpoint, Class<T> responseClass) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + endpoint))
                    .timeout(Duration.ofSeconds(5))
                    .GET();

            String token = resolveBearerToken(null);
            if (token != null && !token.isBlank()) {
                builder.header("Authorization", token.startsWith("Bearer ") ? token : "Bearer " + token);
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

    private <T> ApiResponse<T> executeGet(String endpoint, TypeReference<T> typeRef) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + endpoint))
                    .timeout(Duration.ofSeconds(5))
                    .GET();

            String token = resolveBearerToken(null);
            if (token != null && !token.isBlank()) {
                builder.header("Authorization", token.startsWith("Bearer ") ? token : "Bearer " + token);
            }

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());

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
                    .header("X-Totem-Terminal-Key", "CPMA-TOTEM-KIOSK-TERMINAL-01-SP-GOV-AUTH-2026")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

            String token = resolveBearerToken(bearerToken);
            if (token != null && !token.isBlank()) {
                builder.header("Authorization", token.startsWith("Bearer ") ? token : "Bearer " + token);
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

    private <T> ApiResponse<T> executePut(String endpoint, Object body, Class<T> responseClass, String bearerToken) {
        try {
            String jsonBody = objectMapper.writeValueAsString(body);

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + endpoint))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody));

            String token = resolveBearerToken(bearerToken);
            if (token != null && !token.isBlank()) {
                builder.header("Authorization", token.startsWith("Bearer ") ? token : "Bearer " + token);
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

    private <T> ApiResponse<T> executePostList(String endpoint, Object body, TypeReference<T> typeRef, String bearerToken) {
        try {
            String jsonBody = objectMapper.writeValueAsString(body);

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + endpoint))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

            String token = resolveBearerToken(bearerToken);
            if (token != null && !token.isBlank()) {
                builder.header("Authorization", token.startsWith("Bearer ") ? token : "Bearer " + token);
            }

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());

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

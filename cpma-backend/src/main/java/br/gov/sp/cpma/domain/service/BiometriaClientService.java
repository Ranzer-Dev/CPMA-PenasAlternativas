package br.gov.sp.cpma.domain.service;

import br.gov.sp.cpma.api.exception.DomainException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class BiometriaClientService {

    private final String facialServiceUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public BiometriaClientService(
            @Value("${cpma.facial.service.url:http://127.0.0.1:8001}") String facialServiceUrl,
            ObjectMapper objectMapper) {
        this.facialServiceUrl = facialServiceUrl;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    public BiometriaCompareResult compararFaces(String imagemABase64, String imagemBBase64, double threshold) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("imageA", imagemABase64);
            payload.put("imageB", imagemBBase64);
            payload.put("threshold", threshold);

            String requestBody = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(facialServiceUrl + "/compare"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new DomainException(
                        "BIOMETRIC_SERVICE_ERROR",
                        "Falha ao processar comparacao facial no servico biometrico: HTTP " + response.statusCode(),
                        HttpStatus.BAD_GATEWAY
                );
            }

            JsonNode root = objectMapper.readTree(response.body());
            boolean match = root.path("match").asBoolean(false);
            double confidence = root.path("confidence").asDouble(0.0);

            return new BiometriaCompareResult(match, confidence);
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            throw new DomainException(
                    "BIOMETRIC_SERVICE_UNAVAILABLE",
                    "O servico local de biometria facial nao esta disponivel ou nao respondeu: " + e.getMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        }
    }

    public static class BiometriaCompareResult {
        private final boolean match;
        private final double confidence;

        public BiometriaCompareResult(boolean match, double confidence) {
            this.match = match;
            this.confidence = confidence;
        }

        public boolean isMatch() {
            return match;
        }

        public double getConfidence() {
            return confidence;
        }
    }
}

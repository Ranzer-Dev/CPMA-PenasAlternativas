package br.gov.sp.cpma;

import br.gov.sp.cpma.api.exception.DomainException;
import br.gov.sp.cpma.domain.service.TokenService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

public class TokenServiceTest {

    private static final String SECRET_LONGO = "chave-secreta-para-testes-unitarios-jwt-cpma-backend-2026";

    @Test
    @DisplayName("Deve gerar e validar token de terminal totem com claims corretos")
    void deveGerarEValidarTokenTotem() {
        TokenService tokenService = new TokenService(SECRET_LONGO, 24);

        String token = tokenService.gerarTokenTotem("TOTEM_EXTERNO_01", 1L);
        assertNotNull(token);
        assertFalse(token.isBlank());

        Claims claims = tokenService.validarTokenTotem(token);
        assertEquals("TOTEM_EXTERNO_01", claims.getSubject());
        assertEquals("TOTEM", claims.get("type"));
        assertEquals(1, ((Number) claims.get("adminId")).longValue());
    }

    @Test
    @DisplayName("Deve validar token totem com prefixo Bearer")
    void deveValidarTokenTotemComPrefixoBearer() {
        TokenService tokenService = new TokenService(SECRET_LONGO, 24);

        String token = tokenService.gerarTokenTotem("TOTEM_HALL", 2L);
        Claims claims = tokenService.validarTokenTotem("Bearer " + token);

        assertEquals("TOTEM_HALL", claims.getSubject());
    }

    @Test
    @DisplayName("Deve lancar excecao quando token totem for nulo ou vazio")
    void deveLancarExcecaoQuandoTokenForNuloOuVazio() {
        TokenService tokenService = new TokenService(SECRET_LONGO, 24);

        DomainException exNull = assertThrows(DomainException.class, () -> tokenService.validarTokenTotem(null));
        assertEquals("TOKEN_REQUIRED", exNull.getCode());
        assertEquals(HttpStatus.UNAUTHORIZED, exNull.getStatus());

        DomainException exBlank = assertThrows(DomainException.class, () -> tokenService.validarTokenTotem("   "));
        assertEquals("TOKEN_REQUIRED", exBlank.getCode());
    }

    @Test
    @DisplayName("Deve rejeitar token quando tipo claim for incompativel com TOTEM")
    void deveRejeitarTokenComTipoInvalido() {
        TokenService tokenService = new TokenService(SECRET_LONGO, 24);

        String tokenAdmin = tokenService.gerarTokenAdmin(1L, "12345678900", "Admin Teste", 1);
        DomainException ex = assertThrows(DomainException.class, () -> tokenService.validarTokenTotem(tokenAdmin));

        assertEquals("INVALID_TOKEN_TYPE", ex.getCode());
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    @DisplayName("Deve rejeitar token com assinatura corrompida")
    void deveRejeitarTokenCorrompido() {
        TokenService tokenService = new TokenService(SECRET_LONGO, 24);

        String token = tokenService.gerarTokenTotem("TOTEM_01", 1L);
        String tokenAdulterado = token.substring(0, token.length() - 5) + "abcde";

        DomainException ex = assertThrows(DomainException.class, () -> tokenService.validarTokenTotem(tokenAdulterado));
        assertEquals("INVALID_TOKEN", ex.getCode());
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }

    @Test
    @DisplayName("Deve gerar token admin com claims estruturados")
    void deveGerarTokenAdmin() {
        TokenService tokenService = new TokenService(SECRET_LONGO, 12);

        String token = tokenService.gerarTokenAdmin(10L, "98765432100", "Coordenador", 2);
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    @DisplayName("Deve inicializar com chave curta aplicando padding de seguranca de 32 bytes")
    void deveInicializarComChaveCurta() {
        TokenService tokenService = new TokenService("chave-curta", 24);
        String token = tokenService.gerarTokenTotem("TOTEM_TESTE", 1L);

        Claims claims = tokenService.validarTokenTotem(token);
        assertEquals("TOTEM_TESTE", claims.getSubject());
    }
}

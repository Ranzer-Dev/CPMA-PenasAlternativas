package br.gov.sp.cpma.domain.service;

import br.gov.sp.cpma.api.exception.DomainException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class TokenService {

    private final SecretKey key;
    private final long expirationHours;

    public TokenService(
            @Value("${cpma.security.jwt.secret}") String secret,
            @Value("${cpma.security.jwt.expiration-hours:24}") long expirationHours) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(secretBytes, 0, padded, 0, Math.min(secretBytes.length, 32));
            this.key = Keys.hmacShaKeyFor(padded);
        } else {
            this.key = Keys.hmacShaKeyFor(secretBytes);
        }
        this.expirationHours = expirationHours;
    }

    public String gerarTokenTotem(String terminalId, Long adminId) {
        Instant now = Instant.now();
        Instant expiry = now.plus(expirationHours, ChronoUnit.HOURS);

        return Jwts.builder()
                .subject(terminalId)
                .claim("type", "TOTEM")
                .claim("adminId", adminId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    public String gerarTokenAdmin(Long adminId, String cpf, String nome, int nivelPermissao) {
        Instant now = Instant.now();
        Instant expiry = now.plus(expirationHours, ChronoUnit.HOURS);

        return Jwts.builder()
                .subject(String.valueOf(adminId))
                .claim("type", "ADMIN")
                .claim("cpf", cpf)
                .claim("nome", nome)
                .claim("nivelPermissao", nivelPermissao)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    public Claims validarTokenTotem(String token) {
        if (token == null || token.isBlank()) {
            throw new DomainException("TOKEN_REQUIRED", "O token de autorizacao do totem e obrigatorio.", HttpStatus.UNAUTHORIZED);
        }

        String rawToken = token.startsWith("Bearer ") ? token.substring(7).trim() : token.trim();

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(rawToken)
                    .getPayload();

            String type = claims.get("type", String.class);
            if (!"TOTEM".equals(type)) {
                throw new DomainException("INVALID_TOKEN_TYPE", "O token fornecido nao e valido para o terminal totem.", HttpStatus.FORBIDDEN);
            }

            return claims;
        } catch (ExpiredJwtException ex) {
            throw new DomainException("TOKEN_EXPIRED", "O token do totem expirou. Solicite um novo token ao administrador.", HttpStatus.UNAUTHORIZED);
        } catch (JwtException ex) {
            throw new DomainException("INVALID_TOKEN", "O token do totem e invalido ou foi corrompido.", HttpStatus.UNAUTHORIZED);
        }
    }
}

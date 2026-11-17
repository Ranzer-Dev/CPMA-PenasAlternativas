package br.gov.sp.cpma.api.security;

import br.gov.sp.cpma.domain.entity.Pena;
import br.gov.sp.cpma.domain.repository.PenaRepository;
import br.gov.sp.cpma.domain.service.TokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

public class AdminAuthFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final PenaRepository penaRepository;

    public AdminAuthFilter(TokenService tokenService) {
        this(tokenService, null);
    }

    public AdminAuthFilter(TokenService tokenService, PenaRepository penaRepository) {
        this.tokenService = tokenService;
        this.penaRepository = penaRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        String subPath = path;
        if (subPath.startsWith("/api/v1")) {
            subPath = subPath.substring("/api/v1".length());
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isBlank()) {
            enviarRespostaErro(response, HttpServletResponse.SC_UNAUTHORIZED, "AUTH_REQUIRED",
                    "Acesso negado: Autenticação obrigatória (Token ausente).");
            return;
        }

        Claims claims;
        try {
            claims = tokenService.validarToken(authHeader);
        } catch (Exception ex) {
            enviarRespostaErro(response, HttpServletResponse.SC_UNAUTHORIZED, "INVALID_TOKEN",
                    "Token inválido ou expirado: " + ex.getMessage());
            return;
        }

        String type = claims.get("type", String.class);
        if ("ADMIN".equals(type)) {
            filterChain.doFilter(request, response);
            return;
        }

        if ("APENADO_SESSION".equals(type)) {
            Number numUsuarioId = claims.get("usuarioId", Number.class);
            Long usuarioIdToken = numUsuarioId != null ? numUsuarioId.longValue() : null;
            if (isAcessoPermitidoApenado(subPath, request.getMethod(), usuarioIdToken)) {
                filterChain.doFilter(request, response);
                return;
            }
            enviarRespostaErro(response, HttpServletResponse.SC_FORBIDDEN, "ACCESS_DENIED",
                    "Acesso negado: Sessão de Totem limitada exclusivamente aos dados do próprio apenado.");
            return;
        }

        enviarRespostaErro(response, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN",
                "Perfil de acesso não autorizado.");
    }

    private boolean isAcessoPermitidoApenado(String subPath, String method, Long usuarioIdToken) {
        if (!"GET".equalsIgnoreCase(method) || usuarioIdToken == null) {
            return false;
        }

        if (subPath.equals("/usuarios/" + usuarioIdToken)) {
            return true;
        }

        if (subPath.equals("/penas/usuario/" + usuarioIdToken)) {
            return true;
        }

        if (subPath.startsWith("/registros-trabalho/pena/")) {
            Long penaId = extrairPenaId(subPath.substring("/registros-trabalho/pena/".length()));
            return isPenaDoUsuario(penaId, usuarioIdToken);
        }

        if (subPath.startsWith("/penas/") && subPath.endsWith("/resumo")) {
            String meio = subPath.substring("/penas/".length(), subPath.length() - "/resumo".length());
            Long penaId = extrairPenaId(meio);
            return isPenaDoUsuario(penaId, usuarioIdToken);
        }

        return false;
    }

    private Long extrairPenaId(String segmento) {
        if (segmento == null || segmento.isBlank()) {
            return null;
        }
        String idStr = segmento.split("/")[0];
        try {
            return Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean isPenaDoUsuario(Long penaId, Long usuarioIdToken) {
        if (penaId == null) {
            return false;
        }
        if (penaRepository == null) {
            return true;
        }
        Optional<Pena> optPena = penaRepository.findById(penaId);
        return optPena.isPresent()
                && optPena.get().getUsuario() != null
                && usuarioIdToken.equals(optPena.get().getUsuario().getIdUsuario());
    }

    private void enviarRespostaErro(HttpServletResponse response, int status, String code, String mensagem)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format(
                "{\"status\":%d,\"error\":\"%s\",\"code\":\"%s\",\"mensagem\":\"%s\"}",
                status, status == 401 ? "UNAUTHORIZED" : "FORBIDDEN", code, mensagem
        ));
    }
}

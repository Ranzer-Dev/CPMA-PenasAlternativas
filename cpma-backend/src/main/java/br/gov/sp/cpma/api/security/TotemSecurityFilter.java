package br.gov.sp.cpma.api.security;

import br.gov.sp.cpma.domain.entity.LogAcessoApenado;
import br.gov.sp.cpma.domain.repository.LogAcessoApenadoRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
public class TotemSecurityFilter extends OncePerRequestFilter {

    @Value("${cpma.totem.terminal-key:CPMA-TOTEM-KIOSK-TERMINAL-01-SP-GOV-AUTH-2026}")
    private String terminalKey;

    @Value("${cpma.totem.ip-filter-enabled:true}")
    private boolean ipFilterEnabled;

    @Value("${cpma.totem.allowed-ips:127.0.0.1,0:0:0:0:0:0:0:1,localhost,::1}")
    private String allowedIps;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private LogAcessoApenadoRepository logAcessoRepository;

    public TotemSecurityFilter() {
    }

    public TotemSecurityFilter(LogAcessoApenadoRepository logAcessoRepository) {
        this.logAcessoRepository = logAcessoRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String subPath = path;
        if (subPath.startsWith("/api/v1")) {
            subPath = subPath.substring("/api/v1".length());
        }

        if (!subPath.startsWith("/totem")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (subPath.equals("/totem/status") || subPath.equals("/totem/codigo-temporario")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = extrairIpCliente(request);

        if (ipFilterEnabled && !isIpPermitido(clientIp)) {
            registrarLogTentativa(clientIp, "REDE_NAO_AUTORIZADA",
                    "Acesso bloqueado: IP de rede externa ou Wi-Fi não homologado (" + clientIp + ")");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            if (subPath.endsWith(".html") || subPath.equals("/totem") || subPath.equals("/totem/")) {
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().write(gerarHtmlBloqueioIp(clientIp));
            } else {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"status\":403,\"error\":\"FORBIDDEN\",\"code\":\"UNAUTHORIZED_NETWORK_IP\",\"mensagem\":\"Acesso bloqueado: Dispositivo conectado em rede não autorizada (Wi-Fi ou IP não homologado).\"}");
            }
            return;
        }

        if (isEndpointOperacaoTotem(subPath)) {
            String headerKey = request.getHeader("X-Totem-Terminal-Key");
            if (headerKey == null || !headerKey.equals(terminalKey)) {
                registrarLogTentativa(clientIp, "TERMINAL_NAO_AUTORIZADO",
                        "Acesso bloqueado: Dispositivo sem Chave de Terminal Oficial (X-Totem-Terminal-Key ausente ou inválida). IP: " + clientIp);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"status\":403,\"error\":\"FORBIDDEN\",\"code\":\"UNAUTHORIZED_TERMINAL_DEVICE\",\"mensagem\":\"Acesso bloqueado: Dispositivo não homologado como Totem oficial da CPMA (Chave de Terminal ausente ou inválida).\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isEndpointOperacaoTotem(String subPath) {
        return subPath.startsWith("/totem/etapa1")
                || subPath.startsWith("/totem/etapa2")
                || subPath.startsWith("/totem/validar-codigo")
                || subPath.startsWith("/totem/reconhecer")
                || subPath.startsWith("/totem/token");
    }

    private String extrairIpCliente(HttpServletRequest request) {
        String xForwarded = request.getHeader("X-Forwarded-For");
        if (xForwarded != null && !xForwarded.isBlank()) {
            return xForwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private boolean isIpPermitido(String ip) {
        if (ip == null || ip.isBlank()) return false;
        if (allowedIps == null || allowedIps.isBlank()) return false;
        for (String allowed : allowedIps.split(",")) {
            String trimmed = allowed.trim();
            if (trimmed.equals("*") || trimmed.equalsIgnoreCase(ip)) {
                return true;
            }
            if ("localhost".equalsIgnoreCase(trimmed)
                    && ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip))) {
                return true;
            }
        }
        return false;
    }

    private void registrarLogTentativa(String ip, String metodo, String mensagem) {
        if (logAcessoRepository != null) {
            try {
                LogAcessoApenado log = new LogAcessoApenado();
                log.setMetodo(metodo);
                log.setSucesso(0);
                log.setMensagem(mensagem);
                logAcessoRepository.save(log);
            } catch (Exception ignored) {
            }
        }
    }

    private String gerarHtmlBloqueioIp(String ip) {
        return "<!DOCTYPE html><html lang='pt-BR'><head><meta charset='UTF-8'><title>Acesso Bloqueado - CPMA</title>" +
                "<style>body{background:#0f172a;color:#f8fafc;font-family:sans-serif;display:flex;align-items:center;justify-content:center;height:100vh;margin:0;}" +
                ".card{background:#1e293b;padding:40px;border-radius:12px;border:1px solid #ef4444;text-align:center;max-width:480px;box-shadow:0 10px 25px rgba(0,0,0,0.5);}" +
                "h1{color:#ef4444;font-size:24px;margin-bottom:12px;}p{color:#94a3b8;font-size:14px;line-height:1.6;}" +
                ".badge{background:#450a0a;color:#fca5a5;padding:6px 12px;border-radius:6px;font-size:12px;display:inline-block;margin-top:16px;}</style></head>" +
                "<body><div class='card'><h1>ACESSO BLOQUEADO</h1>" +
                "<p>Este terminal ou rede não está autorizado a acessar o sistema de autoatendimento da CPMA.</p>" +
                "<p>O acesso ao Totem é restrito aos terminais físicos oficiais conectados na rede cabeada homologada.</p>" +
                "<div class='badge'>Origem: " + ip + " | Tentativa registrada no log de auditoria</div>" +
                "</div></body></html>";
    }
}

package br.gov.sp.cpma.api.security;

import br.gov.sp.cpma.domain.service.TokenService;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class SecurityConfig {

    @Bean
    public FilterRegistrationBean<AdminAuthFilter> adminAuthFilterRegistration(TokenService tokenService) {
        FilterRegistrationBean<AdminAuthFilter> registration = new FilterRegistrationBean<>(new AdminAuthFilter(tokenService));
        registration.addUrlPatterns(
                "/usuarios",
                "/usuarios/*",
                "/api/v1/usuarios",
                "/api/v1/usuarios/*",
                "/penas",
                "/penas/*",
                "/api/v1/penas",
                "/api/v1/penas/*",
                "/instituicoes",
                "/instituicoes/*",
                "/api/v1/instituicoes",
                "/api/v1/instituicoes/*",
                "/registros-trabalho",
                "/registros-trabalho/*",
                "/api/v1/registros-trabalho",
                "/api/v1/registros-trabalho/*",
                "/auditoria",
                "/auditoria/*",
                "/api/v1/auditoria",
                "/api/v1/auditoria/*",
                "/totem/codigo-temporario",
                "/api/v1/totem/codigo-temporario"
        );
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 60);
        return registration;
    }
}

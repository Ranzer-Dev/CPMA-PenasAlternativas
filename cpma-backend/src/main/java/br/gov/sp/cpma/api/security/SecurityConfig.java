package br.gov.sp.cpma.api.security;

import br.gov.sp.cpma.domain.repository.PenaRepository;
import br.gov.sp.cpma.domain.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(false);
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Totem-Terminal-Key",
                "Accept",
                "Origin",
                "X-Requested-With"
        ));
        config.setExposedHeaders(List.of("Authorization"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<AdminAuthFilter> adminAuthFilterRegistration(
            TokenService tokenService,
            @Autowired(required = false) PenaRepository penaRepository) {
        FilterRegistrationBean<AdminAuthFilter> registration = new FilterRegistrationBean<>(
                new AdminAuthFilter(tokenService, penaRepository)
        );
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

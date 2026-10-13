package dev.pedroreis.rest_skeleton_java.config;

import dev.pedroreis.rest_skeleton_java.auth.adapters.inbound.JwtAuthenticationFilter;
import dev.pedroreis.rest_skeleton_java.auth.adapters.inbound.RestAccessDeniedHandler;
import dev.pedroreis.rest_skeleton_java.auth.adapters.inbound.RestAuthenticationEntryPoint;
import dev.pedroreis.rest_skeleton_java.auth.ports.outbound.TokenPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Segurança web (ver ADR 006): sem estado, token no header, tudo protegido exceto {@code /api/v1/public/**}.
 * CSRF fica desligado porque não há cookie de sessão; quem copiar esta configuração para um projeto com
 * cookie precisa reavaliar.
 */
@Configuration
public class SecurityConfig {
    private static final String PUBLIC_PATHS = "/api/v1/public/**";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, TokenPort tokenPort, ObjectMapper objectMapper)
            throws Exception {
        return http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(new RestAuthenticationEntryPoint(objectMapper))
                        .accessDeniedHandler(new RestAccessDeniedHandler(objectMapper)))
                .addFilterBefore(new JwtAuthenticationFilter(tokenPort), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /** Origens vêm de {@code api.cors.allowed-origins}; vazia (padrão) não libera nenhuma. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${api.cors.allowed-origins:}") List<String> allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins.stream().filter(origin -> !origin.isBlank()).toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

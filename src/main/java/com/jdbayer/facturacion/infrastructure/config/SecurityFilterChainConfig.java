package com.jdbayer.facturacion.infrastructure.config;

import com.jdbayer.facturacion.infrastructure.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authorization.HttpStatusServerAccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración de seguridad para Spring WebFlux.
 *
 * Esta clase configura:
 * - Autenticación basada en JWT
 * - Autorización de endpoints
 * - CORS
 * - Manejo de errores de seguridad
 *
 * @EnableWebFluxSecurity: Habilita seguridad para WebFlux
 * @EnableReactiveMethodSecurity: Habilita @PreAuthorize, @PostAuthorize, etc.
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityFilterChainConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityFilterChainConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Configura la cadena de filtros de seguridad.
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // Deshabilitar CSRF (no necesario para APIs REST stateless con JWT)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(Customizer.withDefaults())

                // Deshabilitar form login (usamos JWT)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)

                // Deshabilitar HTTP Basic (usamos JWT)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)

                // Deshabilitar logout por defecto (manejamos invalidación de token)
                .logout(ServerHttpSecurity.LogoutSpec::disable)

                // Configurar autorización de endpoints
                .authorizeExchange(auth -> auth
                        // Rutas públicas - NO requieren autenticación
                        .pathMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()

                        // Actuator endpoints (health, info)
                        .pathMatchers("/actuator/health").permitAll()
                        .pathMatchers("/actuator/info").permitAll()

                        // Swagger/OpenAPI (si se configura)
                        .pathMatchers("/swagger-ui/**").permitAll()
                        .pathMatchers("/v3/api-docs/**").permitAll()

                        // Todas las demás rutas requieren autenticación
                        .anyExchange().authenticated()
                )

                // Agregar filtro JWT antes del filtro de autenticación
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)

                // Manejo de errores de autenticación (401 Unauthorized)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler(new HttpStatusServerAccessDeniedHandler(HttpStatus.FORBIDDEN))
                )

                .build();
    }

    /**
     * Bean para manejar CORS globalmente (alternativa).
     * Si prefieres configurar CORS aquí en lugar de WebFluxConfig.
     */

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

}
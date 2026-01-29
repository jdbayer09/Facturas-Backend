package com.jdbayer.facturacion.infrastructure.security.jwt;

import com.jdbayer.facturacion.domain.exception.UserNotFoundException;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.UUID;

/**
 * Filtro WebFlux para autenticación basada en JWT.
 *
 * Este filtro se ejecuta en cada request y:
 * 1. Extrae el token JWT del header Authorization
 * 2. Valida el token
 * 3. Extrae la información del usuario
 * 4. Establece el contexto de seguridad de Spring
 *
 * Flujo:
 * Request → JwtAuthenticationFilter → Validar Token → Set SecurityContext → Controller
 */
@Component
public class JwtAuthenticationFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public JwtAuthenticationFilter(JwtService jwtService, JwtProperties jwtProperties) {
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public @NullMarked Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // Permitir rutas públicas sin validación JWT
        if (isPublicPath(path)) {
            log.debug("Ruta pública, omitiendo validación JWT: {}", path);
            return chain.filter(exchange);
        }

        // Extraer token del header
        String token = extractToken(request);

        if (token == null) {
            log.debug("No se encontró token JWT en el request");
            return chain.filter(exchange);
        }

        // Validar y procesar token
        return validateAndSetAuthentication(token)
                .flatMap(authentication ->
                        chain.filter(exchange)
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
                );
    }

    /**
     * Valida el token y crea el objeto Authentication.
     */
    private Mono<UsernamePasswordAuthenticationToken> validateAndSetAuthentication(String token) {
        return Mono.fromCallable(() -> {
            if (!jwtService.validateToken(token)) {
                log.warn("Token JWT inválido o expirado");
                throw new IllegalStateException("Invalid JWT");
            }

            // Extraer información del usuario del token
            UUID userId = jwtService.extractUserId(token);
            String email = jwtService.extractEmail(token);

            log.debug("Token JWT válido para usuario: {} ({})", email, userId);

            // Crear Authentication con autoridades
            // Por ahora, todos tienen rol USER
            // Más adelante se pueden extraer roles del token o BD
            var authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

            // Crear objeto de autenticación
            // Principal: userId (puede ser un objeto UserDetails personalizado)
            // Credentials: token (no se usa típicamente)
            // Authorities: roles/permisos
            return new UsernamePasswordAuthenticationToken(
                    userId,
                    token,
                    authorities
            );
        }).onErrorResume(e -> {
            log.error("Error al procesar token JWT: {}", e.getMessage());
            return Mono.empty();
        });
    }

    /**
     * Extrae el token JWT del header Authorization.
     *
     * Formato esperado: "Bearer <token>"
     */
    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith(jwtProperties.getHeaderPrefix())) {
            return authHeader.substring(jwtProperties.getHeaderPrefix().length());
        }

        return null;
    }

    /**
     * Verifica si una ruta es pública y no requiere autenticación.
     */
    private boolean isPublicPath(String path) {
        return path.startsWith("/api/auth/") ||
                path.equals("/actuator/health") ||
                path.equals("/actuator/info") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs");
    }
}
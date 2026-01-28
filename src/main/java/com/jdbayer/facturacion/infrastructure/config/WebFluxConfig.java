package com.jdbayer.facturacion.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * Configuración de WebFlux.
 *
 * Configura aspectos de Spring WebFlux como:
 * - CORS (Cross-Origin Resource Sharing)
 * - Path matching
 * - Message converters
 * - View resolvers
 */
@Configuration
@EnableWebFlux
public class WebFluxConfig implements WebFluxConfigurer {

    /**
     * Configura CORS para permitir requests desde diferentes orígenes.
     *
     * CORS es necesario cuando el frontend (ej: React, Angular, Vue)
     * está en un dominio diferente al backend.
     *
     * Ejemplo:
     * - Frontend: http://localhost:3000
     * - Backend: http://localhost:8080
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // Orígenes permitidos (en producción, especificar dominios reales)
                .allowedOrigins(
                        "http://localhost:3000",  // React default
                        "http://localhost:4200",  // Angular default
                        "http://localhost:5173",  // Vite default
                        "http://localhost:8080"   // Mismo origen
                )
                // Métodos HTTP permitidos
                .allowedMethods(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "PATCH",
                        "OPTIONS"
                )
                // Headers permitidos
                .allowedHeaders(
                        "Authorization",
                        "Content-Type",
                        "Accept",
                        "X-Requested-With"
                )
                // Headers expuestos al cliente
                .exposedHeaders(
                        "Authorization",
                        "X-Total-Count"
                )
                // Permitir credenciales (cookies, authorization headers)
                .allowCredentials(true)
                // Tiempo de caché de la configuración CORS (en segundos)
                .maxAge(3600);
    }

    /**
     * Aquí puedes agregar otras configuraciones de WebFlux:
     *
     * - Custom message converters
     * - Path matching configuration
     * - Resource handlers
     * - View resolvers
     */
}
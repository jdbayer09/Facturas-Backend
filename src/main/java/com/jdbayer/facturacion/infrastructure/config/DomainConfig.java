package com.jdbayer.facturacion.infrastructure.config;

import com.jdbayer.facturacion.domain.repository.UserRepository;
import com.jdbayer.facturacion.domain.security.PasswordEncoder;
import com.jdbayer.facturacion.domain.service.UserDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de beans del dominio.
 *
 * Esta clase pertenece a la capa de infraestructura porque:
 * - Usa anotaciones de Spring (@Configuration, @Bean)
 * - Conecta las abstracciones del dominio con implementaciones concretas
 * - Es responsabilidad de la infraestructura "cablear" las dependencias
 *
 * El dominio permanece puro (sin dependencias de Spring).
 */
@Configuration
public class DomainConfig {

    /**
     * Crea el bean de UserDomainService.
     * Spring inyectará automáticamente las dependencias (UserRepository y PasswordEncoder)
     * desde sus respectivas configuraciones.
     */
    @Bean
    public UserDomainService userDomainService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return new UserDomainService(userRepository, passwordEncoder);
    }
}
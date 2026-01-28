package com.jdbayer.facturacion.infrastructure.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.transaction.ReactiveTransactionManager;

/**
 * Configuración de R2DBC (Reactive Relational Database Connectivity).
 *
 * R2DBC es la alternativa reactiva a JDBC, diseñada para trabajar
 * con bases de datos relacionales de forma no bloqueante.
 *
 * Características:
 * - Non-blocking I/O
 * - Backpressure support
 * - Compatible con Project Reactor
 * - Alto rendimiento en aplicaciones reactivas
 *
 * @EnableR2dbcRepositories: Habilita repositorios R2DBC
 * @EnableR2dbcAuditing: Habilita auditoría automática (@CreatedDate, @LastModifiedDate)
 */
@Configuration
@EnableR2dbcRepositories(basePackages = "com.jdbayer.facturacion.infrastructure.persistence.repository")
@EnableR2dbcAuditing
public class R2dbcConfig {

    /**
     * Configura el Transaction Manager para R2DBC.
     *
     * Permite usar @Transactional en métodos reactivos.
     */
    @Bean
    public ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

    /**
     * Inicializador de la base de datos (opcional).
     *
     * NOTA: Para migraciones de BD, es mejor usar Flyway (ya configurado).
     * Este bean es útil solo para inicialización en desarrollo/testing.
     */
    @Bean
    public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);

        // No necesitamos scripts SQL aquí porque usamos Flyway
        // Flyway se encarga de ejecutar los scripts de migración automáticamente

        return initializer;
    }

    /**
     * Configuración adicional de R2DBC (opcional).
     *
     * Aquí puedes personalizar:
     * - Custom converters
     * - Naming strategies
     * - Logging
     */
}
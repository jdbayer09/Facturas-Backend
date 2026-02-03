package com.jdbayer.facturacion.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuración para habilitar tareas programadas (schedulers).
 *
 * @EnableScheduling permite el uso de @Scheduled en los componentes.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // Esta clase solo necesita la anotación @EnableScheduling
    // No requiere configuración adicional para el uso básico
}
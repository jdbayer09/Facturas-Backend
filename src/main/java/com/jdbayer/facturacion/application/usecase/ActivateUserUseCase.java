package com.jdbayer.facturacion.application.usecase;

import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Caso de uso: Activar un usuario.
 *
 * Responsabilidades:
 * - Validar que el usuario exista
 * - Activar el usuario
 * - Persistir los cambios
 */
public interface ActivateUserUseCase {
    Mono<Void> execute(UUID userId);
}
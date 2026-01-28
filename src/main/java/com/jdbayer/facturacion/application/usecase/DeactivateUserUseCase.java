package com.jdbayer.facturacion.application.usecase;

import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Caso de uso: Desactivar un usuario.
 *
 * Responsabilidades:
 * - Validar que el usuario exista
 * - Validar que pueda ser desactivado
 * - Desactivar el usuario
 * - Persistir los cambios
 */
public interface DeactivateUserUseCase {
    Mono<Void> execute(UUID userId);
}
package com.jdbayer.facturacion.application.usecase;

import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Caso de uso: Cerrar sesión (logout).
 *
 * Responsabilidades:
 * - Invalidar el access token actual (blacklist)
 * - Invalidar el refresh token asociado
 * - Registrar el evento de logout
 */
public interface LogoutUseCase {
    Mono<Void> execute(String token, UUID userId, String ipAddress);
}
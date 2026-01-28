package com.jdbayer.facturacion.application.usecase;

import com.jdbayer.facturacion.application.dto.response.UserResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Caso de uso: Obtener un usuario por su ID.
 *
 * Responsabilidades:
 * - Buscar el usuario
 * - Validar que exista
 * - Retornar la respuesta
 */
public interface GetUserByIdUseCase {
    Mono<UserResponse> execute(UUID userId);
}
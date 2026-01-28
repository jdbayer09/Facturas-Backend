package com.jdbayer.facturacion.application.usecase;

import com.jdbayer.facturacion.application.dto.request.UpdateUserRequest;
import com.jdbayer.facturacion.application.dto.response.UserResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Caso de uso: Actualizar información de un usuario.
 *
 * Responsabilidades:
 * - Validar que el usuario exista
 * - Validar que el nuevo email no esté duplicado (si se cambia)
 * - Actualizar la información
 * - Persistir los cambios
 * - Retornar la respuesta
 */
public interface UpdateUserUseCase {
    Mono<UserResponse> execute(UUID userId, UpdateUserRequest request);
}
package com.jdbayer.facturacion.application.usecase;

import com.jdbayer.facturacion.application.dto.request.RegisterUserRequest;
import com.jdbayer.facturacion.application.dto.response.UserResponse;
import reactor.core.publisher.Mono;

/**
 * Caso de uso: Registrar un nuevo usuario.
 *
 * Responsabilidades:
 * - Validar que el email no esté duplicado
 * - Crear el hash de la contraseña
 * - Crear la entidad User
 * - Persistir el usuario
 * - Retornar la respuesta
 */
public interface RegisterUserUseCase {
    Mono<UserResponse> execute(RegisterUserRequest request);
}
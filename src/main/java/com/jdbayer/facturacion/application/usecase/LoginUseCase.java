package com.jdbayer.facturacion.application.usecase;

import com.jdbayer.facturacion.application.dto.request.LoginRequest;
import com.jdbayer.facturacion.application.dto.response.AuthResponse;
import reactor.core.publisher.Mono;

/**
 * Caso de uso: Autenticar un usuario.
 *
 * Responsabilidades:
 * - Validar credenciales
 * - Generar token JWT
 * - Retornar la respuesta con el token y la información del usuario
 */
public interface LoginUseCase {
    Mono<AuthResponse> execute(LoginRequest request);
}
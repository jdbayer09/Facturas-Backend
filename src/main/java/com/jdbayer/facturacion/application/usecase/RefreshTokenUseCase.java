package com.jdbayer.facturacion.application.usecase;

import com.jdbayer.facturacion.application.dto.request.RefreshTokenRequest;
import com.jdbayer.facturacion.application.dto.response.AuthResponse;
import reactor.core.publisher.Mono;

/**
 * Caso de uso: Renovar token de acceso usando refresh token.
 *
 * Responsabilidades:
 * - Validar el refresh token
 * - Generar nuevo access token
 * - Generar nuevo refresh token (rotación)
 * - Invalidar el refresh token anterior
 * - Retornar la respuesta con los nuevos tokens
 */
public interface RefreshTokenUseCase {
    Mono<AuthResponse> execute(RefreshTokenRequest request, String ipAddress, String userAgent);
}
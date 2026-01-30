package com.jdbayer.facturacion.application.dto.response;

/**
 * DTO de respuesta para autenticación exitosa.
 * Incluye el access token, refresh token y la información del usuario.
 */
public record AuthResponse(
        String token,
        String tokenType,
        String refreshToken,
        UserResponse user
) {
    public AuthResponse(String token, UserResponse user) {
        this(token, "Bearer", null, user);
    }

    public AuthResponse(String token, String refreshToken, UserResponse user) {
        this(token, "Bearer", refreshToken, user);
    }
}
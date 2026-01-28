package com.jdbayer.facturacion.application.dto.response;

/**
 * DTO de respuesta para autenticación exitosa.
 * Incluye el token JWT y la información del usuario.
 */
public record AuthResponse(
        String token,
        String tokenType,
        UserResponse user
) {
    public AuthResponse(String token, UserResponse user) {
        this(token, "Bearer", user);
    }
}
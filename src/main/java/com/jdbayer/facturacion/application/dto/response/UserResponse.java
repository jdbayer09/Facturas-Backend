package com.jdbayer.facturacion.application.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de respuesta para información del usuario.
 * No expone información sensible como contraseñas.
 */
public record UserResponse(
        UUID id,
        String name,
        String lastName,
        String email,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}
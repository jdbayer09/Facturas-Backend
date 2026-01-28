package com.jdbayer.facturacion.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * DTO para actualizar información del usuario.
 * Todos los campos son opcionales.
 */
public record UpdateUserRequest(

        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String name,

        @Size(max = 100, message = "El apellido no puede exceder 100 caracteres")
        String lastName,

        @Email(message = "Email inválido")
        @Size(max = 100, message = "El email no puede exceder 100 caracteres")
        String email
) {
}
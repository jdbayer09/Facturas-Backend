package com.jdbayer.facturacion.application.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para la solicitud de renovación de token.
 *
 * El refresh token permite obtener un nuevo access token sin
 * volver a introducir las credenciales del usuario.
 */
public record RefreshTokenRequest(

        @NotBlank(message = "El refresh token es obligatorio")
        String refreshToken
) {
}
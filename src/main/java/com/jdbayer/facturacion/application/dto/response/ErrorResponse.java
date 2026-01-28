package com.jdbayer.facturacion.application.dto.response;

import java.time.Instant;
import java.util.Map;

/**
 * DTO estándar para respuestas de error.
 * Proporciona información consistente sobre errores a los clientes.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> validationErrors
) {
    public ErrorResponse(int status, String error, String message, String path) {
        this(Instant.now(), status, error, message, path, null);
    }

    public ErrorResponse(int status, String error, String message, String path, Map<String, String> validationErrors) {
        this(Instant.now(), status, error, message, path, validationErrors);
    }
}
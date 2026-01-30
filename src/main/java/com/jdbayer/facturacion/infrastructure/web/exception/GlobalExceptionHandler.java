package com.jdbayer.facturacion.infrastructure.web.exception;

import com.jdbayer.facturacion.application.dto.response.ErrorResponse;
import com.jdbayer.facturacion.domain.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para toda la aplicación.
 *
 * Convierte excepciones de dominio en respuestas HTTP apropiadas
 * con códigos de estado y mensajes descriptivos.
 *
 * @RestControllerAdvice permite interceptar excepciones de todos los controllers.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones de validación de DTOs (Jakarta Validation).
     *
     * Se activa cuando fallan las validaciones en @Valid.
     *
     * @return 400 BAD REQUEST con detalles de los campos inválidos
     *
     * Ejemplo de response:
     * {
     *   "timestamp": "2025-01-28T10:00:00Z",
     *   "status": 400,
     *   "error": "Bad Request",
     *   "message": "Error de validación",
     *   "path": "/api/auth/register",
     *   "validationErrors": {
     *     "email": "Email inválido",
     *     "password": "La contraseña debe tener entre 8 y 100 caracteres"
     *   }
     * }
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidationException(
            WebExchangeBindException ex,
            ServerWebExchange exchange
    ) {
        log.error("Error de validación: {}", ex.getMessage());

        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                validationErrors.put(error.getField(), error.getDefaultMessage())
        );

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Error de validación",
                exchange.getRequest().getPath().value(),
                validationErrors
        );

        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse));
    }

    /**
     * Maneja excepciones cuando un usuario no es encontrado.
     *
     * @return 404 NOT FOUND
     *
     * Ejemplo de response:
     * {
     *   "timestamp": "2025-01-28T10:00:00Z",
     *   "status": 404,
     *   "error": "Not Found",
     *   "message": "Usuario no encontrado con ID: 123e4567-e89b-12d3-a456-426614174000",
     *   "path": "/api/users/123e4567-e89b-12d3-a456-426614174000"
     * }
     */
    @ExceptionHandler(UserNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleUserNotFoundException(
            UserNotFoundException ex,
            ServerWebExchange exchange
    ) {
        log.error("Usuario no encontrado: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                exchange.getRequest().getPath().value()
        );

        return Mono.just(ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse));
    }

    /**
     * Maneja excepciones de email duplicado.
     *
     * @return 409 CONFLICT
     *
     * Ejemplo de response:
     * {
     *   "timestamp": "2025-01-28T10:00:00Z",
     *   "status": 409,
     *   "error": "Conflict",
     *   "message": "Ya existe un usuario con el email: juan@example.com",
     *   "path": "/api/auth/register"
     * }
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleDuplicateEmailException(
            DuplicateEmailException ex,
            ServerWebExchange exchange
    ) {
        log.error("Email duplicado: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                exchange.getRequest().getPath().value()
        );

        return Mono.just(ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse));
    }

    /**
     * Maneja excepciones de credenciales inválidas.
     *
     * @return 401 UNAUTHORIZED
     *
     * Ejemplo de response:
     * {
     *   "timestamp": "2025-01-28T10:00:00Z",
     *   "status": 401,
     *   "error": "Unauthorized",
     *   "message": "Credenciales inválidas",
     *   "path": "/api/auth/login"
     * }
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInvalidCredentialsException(
            InvalidCredentialsException ex,
            ServerWebExchange exchange
    ) {
        log.error("Credenciales inválidas: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                ex.getMessage(),
                exchange.getRequest().getPath().value()
        );

        return Mono.just(ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse));
    }

    /**
     * Maneja excepciones de email inválido.
     *
     * @return 400 BAD REQUEST
     */
    @ExceptionHandler(InvalidEmailException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInvalidEmailException(
            InvalidEmailException ex,
            ServerWebExchange exchange
    ) {
        log.error("Email inválido: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                exchange.getRequest().getPath().value()
        );

        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse));
    }

    /**
     * Maneja excepciones de nombre inválido.
     *
     * @return 400 BAD REQUEST
     */
    @ExceptionHandler(InvalidNameException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInvalidNameException(
            InvalidNameException ex,
            ServerWebExchange exchange
    ) {
        log.error("Nombre inválido: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                exchange.getRequest().getPath().value()
        );

        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse));
    }

    /**
     * Maneja todas las excepciones de dominio no específicas.
     *
     * @return 400 BAD REQUEST
     */
    @ExceptionHandler(DomainException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleDomainException(
            DomainException ex,
            ServerWebExchange exchange
    ) {
        log.error("Error de dominio: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                exchange.getRequest().getPath().value()
        );

        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse));
    }

    /**
     * Maneja IllegalStateException (errores de lógica de negocio).
     *
     * @return 422 UNPROCESSABLE ENTITY
     *
     * Ejemplo:
     * - Intentar desactivar un usuario ya inactivo
     * - Intentar activar un usuario ya activo
     */
    @ExceptionHandler(IllegalStateException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleIllegalStateException(
            IllegalStateException ex,
            ServerWebExchange exchange
    ) {
        log.error("Estado inválido: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                422,
                "Unprocessable Entity",
                ex.getMessage(),
                exchange.getRequest().getPath().value()
        );

        return Mono.just(ResponseEntity
                .status(HttpStatusCode.valueOf(422))
                .body(errorResponse));
    }

    /**
     * Maneja IllegalArgumentException (argumentos inválidos).
     *
     * @return 400 BAD REQUEST
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleIllegalArgumentException(
            IllegalArgumentException ex,
            ServerWebExchange exchange
    ) {
        log.error("Argumento inválido: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                exchange.getRequest().getPath().value()
        );

        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse));
    }

    /**
     * Maneja excepciones genéricas no contempladas.
     *
     * @return 500 INTERNAL SERVER ERROR
     *
     * IMPORTANTE: En producción, no exponer detalles internos del error.
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(
            Exception ex,
            ServerWebExchange exchange
    ) {
        log.error("Error interno del servidor: ", ex);

        // En producción, usar un mensaje genérico
        String message = "Ha ocurrido un error interno. Por favor, contacte al administrador.";

        // En desarrollo, mostrar el mensaje real
        if (log.isDebugEnabled()) {
            message = ex.getMessage();
        }

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                message,
                exchange.getRequest().getPath().value()
        );

        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse));
    }
}
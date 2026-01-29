package com.jdbayer.facturacion.infrastructure.web.controller;

import com.jdbayer.facturacion.application.dto.request.LoginRequest;
import com.jdbayer.facturacion.application.dto.request.RegisterUserRequest;
import com.jdbayer.facturacion.application.dto.response.AuthResponse;
import com.jdbayer.facturacion.application.dto.response.UserResponse;
import com.jdbayer.facturacion.application.usecase.LoginUseCase;
import com.jdbayer.facturacion.application.usecase.RegisterUserUseCase;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Controlador REST para endpoints de autenticación.
 *
 * Endpoints:
 * - POST /api/auth/register - Registro de nuevos usuarios
 * - POST /api/auth/login - Autenticación de usuarios
 *
 * Estos endpoints son públicos (no requieren autenticación).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;

    public AuthController(
            RegisterUserUseCase registerUserUseCase,
            LoginUseCase loginUseCase
    ) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUseCase = loginUseCase;
    }

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param request Datos del usuario a registrar
     * @return 201 CREATED con los datos del usuario registrado
     *
     * Ejemplo de request:
     * POST /api/auth/register
     * {
     *   "name": "Juan",
     *   "lastName": "Pérez",
     *   "email": "juan@example.com",
     *   "password": "SecurePass123"
     * }
     *
     * Ejemplo de response (201 CREATED):
     * {
     *   "id": "123e4567-e89b-12d3-a456-426614174000",
     *   "name": "JUAN",
     *   "lastName": "PÉREZ",
     *   "email": "juan@example.com",
     *   "active": true,
     *   "createdAt": "2025-01-28T10:00:00Z",
     *   "updatedAt": "2025-01-28T10:00:00Z"
     * }
     */
    @PostMapping("/register")
    public Mono<ResponseEntity<UserResponse>> register(@Valid @RequestBody RegisterUserRequest request) {
        log.info("Solicitud de registro recibida para email: {}", request.email());

        return registerUserUseCase.execute(request)
                .map(userResponse -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(userResponse))
                .doOnSuccess(response ->
                {
                    log.info("Usuario registrado exitosamente: {}", response.getBody().email());
                });
    }

    /**
     * Autentica un usuario y retorna un token JWT.
     *
     * @param request Credenciales del usuario
     * @return 200 OK con el token JWT y datos del usuario
     *
     * Ejemplo de request:
     * POST /api/auth/login
     * {
     *   "email": "juan@example.com",
     *   "password": "SecurePass123"
     * }
     *
     * Ejemplo de response (200 OK):
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "tokenType": "Bearer",
     *   "user": {
     *     "id": "123e4567-e89b-12d3-a456-426614174000",
     *     "name": "JUAN",
     *     "lastName": "PÉREZ",
     *     "email": "juan@example.com",
     *     "active": true
     *   }
     * }
     */
    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Solicitud de login recibida para email: {}", request.email());

        return loginUseCase.execute(request)
                .map(authResponse -> ResponseEntity
                        .ok()
                        .body(authResponse))
                .doOnSuccess(response ->
                        log.info("Login exitoso para: {}", response.getBody().user().email()));
    }

    /**
     * Endpoint para renovar el token de acceso usando un refresh token.
     *
     * NOTA: Este endpoint está preparado para implementación futura.
     * Requiere implementar RefreshTokenUseCase.
     *
     * @param request Refresh token
     * @return 200 OK con nuevo token JWT
     */
    /*
    @PostMapping("/refresh")
    public Mono<ResponseEntity<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Solicitud de refresh token recibida");

        return refreshTokenUseCase.execute(request)
                .map(authResponse -> ResponseEntity
                        .ok()
                        .body(authResponse));
    }
    */

    /**
     * Endpoint para cerrar sesión (invalidar token).
     *
     * NOTA: Este endpoint está preparado para implementación futura.
     * Requiere implementar un mecanismo de blacklist de tokens.
     *
     * @return 204 NO CONTENT
     */
    /*
    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(@RequestHeader("Authorization") String authHeader) {
        log.info("Solicitud de logout recibida");

        return logoutUseCase.execute(authHeader)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
    */
}
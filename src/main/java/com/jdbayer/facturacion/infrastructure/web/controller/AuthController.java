package com.jdbayer.facturacion.infrastructure.web.controller;

import com.jdbayer.facturacion.application.dto.request.LoginRequest;
import com.jdbayer.facturacion.application.dto.request.RefreshTokenRequest;
import com.jdbayer.facturacion.application.dto.request.RegisterUserRequest;
import com.jdbayer.facturacion.application.dto.response.AuthResponse;
import com.jdbayer.facturacion.application.dto.response.UserResponse;
import com.jdbayer.facturacion.application.usecase.LoginUseCase;
import com.jdbayer.facturacion.application.usecase.LogoutUseCase;
import com.jdbayer.facturacion.application.usecase.RefreshTokenUseCase;
import com.jdbayer.facturacion.application.usecase.RegisterUserUseCase;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Controlador REST para endpoints de autenticación.
 *
 * Endpoints:
 * - POST /api/auth/register - Registro de nuevos usuarios
 * - POST /api/auth/login - Autenticación de usuarios
 * - POST /api/auth/refresh - Renovar access token
 * - POST /api/auth/logout - Cerrar sesión
 *
 * Los endpoints de register, login y refresh son públicos.
 * El endpoint de logout requiere autenticación.
 */
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;

    public AuthController(
            RegisterUserUseCase registerUserUseCase,
            LoginUseCase loginUseCase,
            RefreshTokenUseCase refreshTokenUseCase,
            LogoutUseCase logoutUseCase
    ) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUseCase = loginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
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
                        log.info("Usuario registrado exitosamente: {}", response.getBody().email()));
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
     * @param request Refresh token
     * @param httpRequest Request HTTP (para obtener IP y User-Agent)
     * @return 200 OK con nuevo access token y refresh token
     *
     * Ejemplo de request:
     * POST /api/auth/refresh
     * {
     *   "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
     * }
     *
     * Ejemplo de response (200 OK):
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIs...",
     *   "tokenType": "Bearer",
     *   "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
     *   "user": {
     *     "id": "123e4567-e89b-12d3-a456-426614174000",
     *     "name": "JUAN",
     *     "email": "juan@example.com"
     *   }
     * }
     */
    @PostMapping("/refresh")
    public Mono<ResponseEntity<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            ServerHttpRequest httpRequest
    ) {
        log.info("Solicitud de refresh token recibida");

        String ipAddress = extractIpAddress(httpRequest);
        String userAgent = extractUserAgent(httpRequest);

        return refreshTokenUseCase.execute(request, ipAddress, userAgent)
                .map(ResponseEntity::ok)
                .doOnSuccess(response ->
                        log.info("Tokens renovados exitosamente"));
    }

    /**
     * Endpoint para cerrar sesión (logout).
     *
     * Invalida el token actual agregándolo a la blacklist.
     *
     * @param authentication Autenticación del usuario (inyectada automáticamente)
     * @param authHeader Header Authorization
     * @param httpRequest Request HTTP (para obtener IP)
     * @return 204 NO CONTENT
     *
     * Ejemplo de request:
     * POST /api/auth/logout
     * Header: Authorization: Bearer <token>
     *
     * Ejemplo de response: 204 NO CONTENT
     */
    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(
            Authentication authentication,
            @RequestHeader("Authorization") String authHeader,
            ServerHttpRequest httpRequest
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        log.info("Solicitud de logout recibida para usuario: {}", userId);

        // Extraer token del header "Bearer <token>"
        String token = authHeader.substring(7);
        String ipAddress = extractIpAddress(httpRequest);

        return logoutUseCase.execute(token, userId, ipAddress)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .doOnSuccess(v -> log.info("Logout exitoso para usuario: {}", userId));
    }

    /**
     * Extrae la dirección IP del request.
     */
    private String extractIpAddress(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        var remoteAddress = request.getRemoteAddress();
        return remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "unknown";
    }

    /**
     * Extrae el User-Agent del request.
     */
    private String extractUserAgent(ServerHttpRequest request) {
        String userAgent = request.getHeaders().getFirst("User-Agent");
        return userAgent != null ? userAgent : "unknown";
    }
}
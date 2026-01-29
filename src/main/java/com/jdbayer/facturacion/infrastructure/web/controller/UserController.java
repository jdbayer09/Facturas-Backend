package com.jdbayer.facturacion.infrastructure.web.controller;

import com.jdbayer.facturacion.application.dto.request.UpdateUserRequest;
import com.jdbayer.facturacion.application.dto.response.UserResponse;
import com.jdbayer.facturacion.application.usecase.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Controlador REST para gestión de usuarios.
 *
 * Endpoints:
 * - GET /api/users/{id} - Obtener usuario por ID
 * - GET /api/users/me - Obtener usuario autenticado actual
 * - PUT /api/users/{id} - Actualizar usuario
 * - DELETE /api/users/{id} - Desactivar usuario (soft delete)
 * - PUT /api/users/{id}/activate - Activar usuario
 *
 * Todos estos endpoints requieren autenticación (JWT).
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final GetUserByIdUseCase getUserByIdUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeactivateUserUseCase deactivateUserUseCase;
    private final ActivateUserUseCase activateUserUseCase;

    public UserController(
            GetUserByIdUseCase getUserByIdUseCase,
            UpdateUserUseCase updateUserUseCase,
            DeactivateUserUseCase deactivateUserUseCase,
            ActivateUserUseCase activateUserUseCase
    ) {
        this.getUserByIdUseCase = getUserByIdUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.deactivateUserUseCase = deactivateUserUseCase;
        this.activateUserUseCase = activateUserUseCase;
    }

    /**
     * Obtiene los datos del usuario autenticado actual.
     *
     * @param authentication Autenticación del usuario (inyectada automáticamente)
     * @return 200 OK con los datos del usuario
     *
     * Ejemplo de request:
     * GET /api/users/me
     * Header: Authorization: Bearer <token>
     *
     * Ejemplo de response (200 OK):
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
    @GetMapping("/me")
    public Mono<ResponseEntity<UserResponse>> getCurrentUser(Authentication authentication) {
        // El filtro JWT ya estableció el userId como principal
        UUID userId = (UUID) authentication.getPrincipal();

        log.info("Obteniendo datos del usuario autenticado: {}", userId);

        return getUserByIdUseCase.execute(userId)
                .map(ResponseEntity::ok)
                .doOnSuccess(response ->
                        log.debug("Usuario encontrado: {}", response.getBody().email()));
    }

    /**
     * Obtiene un usuario por su ID.
     *
     * @param id UUID del usuario
     * @return 200 OK con los datos del usuario
     *
     * Ejemplo de request:
     * GET /api/users/123e4567-e89b-12d3-a456-426614174000
     * Header: Authorization: Bearer <token>
     *
     * Ejemplo de response (200 OK):
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
    @GetMapping("/{id}")
    public Mono<ResponseEntity<UserResponse>> getUserById(@PathVariable UUID id) {
        log.info("Obteniendo usuario por ID: {}", id);

        return getUserByIdUseCase.execute(id)
                .map(ResponseEntity::ok)
                .doOnSuccess(response ->
                        log.debug("Usuario encontrado: {}", response.getBody().email()));
    }

    /**
     * Actualiza la información de un usuario.
     *
     * Permite actualización parcial: solo los campos proporcionados se actualizan.
     *
     * @param id UUID del usuario
     * @param request Datos a actualizar
     * @return 200 OK con los datos actualizados
     *
     * Ejemplo de request:
     * PUT /api/users/123e4567-e89b-12d3-a456-426614174000
     * Header: Authorization: Bearer <token>
     * {
     *   "name": "Juan Carlos",
     *   "lastName": "Pérez García",
     *   "email": "juanc@example.com"
     * }
     *
     * Ejemplo de response (200 OK):
     * {
     *   "id": "123e4567-e89b-12d3-a456-426614174000",
     *   "name": "JUAN CARLOS",
     *   "lastName": "PÉREZ GARCÍA",
     *   "email": "juanc@example.com",
     *   "active": true,
     *   "createdAt": "2025-01-28T10:00:00Z",
     *   "updatedAt": "2025-01-28T15:30:00Z"
     * }
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<UserResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        log.info("Actualizando usuario: {}", id);

        return updateUserUseCase.execute(id, request)
                .map(ResponseEntity::ok)
                .doOnSuccess(response ->
                        log.info("Usuario actualizado: {}", response.getBody().id()));
    }

    /**
     * Desactiva un usuario (soft delete).
     *
     * El usuario permanece en la BD pero no puede autenticarse.
     *
     * @param id UUID del usuario
     * @return 204 NO CONTENT
     *
     * Ejemplo de request:
     * DELETE /api/users/123e4567-e89b-12d3-a456-426614174000
     * Header: Authorization: Bearer <token>
     *
     * Ejemplo de response: 204 NO CONTENT
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deactivateUser(@PathVariable UUID id) {
        log.info("Desactivando usuario: {}", id);

        return deactivateUserUseCase.execute(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .doOnSuccess(v -> log.info("Usuario desactivado: {}", id));
    }

    /**
     * Activa un usuario previamente desactivado.
     *
     * @param id UUID del usuario
     * @return 204 NO CONTENT
     *
     * Ejemplo de request:
     * PUT /api/users/123e4567-e89b-12d3-a456-426614174000/activate
     * Header: Authorization: Bearer <token>
     *
     * Ejemplo de response: 204 NO CONTENT
     */
    @PatchMapping("/{id}/activate")
    public Mono<ResponseEntity<Void>> activateUser(@PathVariable UUID id) {
        log.info("Activando usuario: {}", id);

        return activateUserUseCase.execute(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .doOnSuccess(v -> log.info("Usuario activado: {}", id));
    }

    /**
     * Endpoint futuro para cambiar contraseña.
     *
     * NOTA: Requiere implementar ChangePasswordUseCase.
     */
    /*
    @PutMapping("/{id}/password")
    public Mono<ResponseEntity<Void>> changePassword(
            @PathVariable UUID id,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        log.info("Cambiando contraseña para usuario: {}", id);

        return changePasswordUseCase.execute(id, request)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
    */
}
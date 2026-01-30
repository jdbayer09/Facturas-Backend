package com.jdbayer.facturacion.infrastructure.security.repository;

import com.jdbayer.facturacion.infrastructure.security.token.RefreshToken;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repositorio REACTIVO Spring Data Redis para RefreshToken.
 *
 * IMPORTANTE: Usa ReactiveCrudRepository en lugar de CrudRepository
 * para retornar Mono/Flux en lugar de Optional/List.
 *
 * Esto es coherente con la arquitectura reactiva (WebFlux).
 */
@Repository
public interface RefreshTokenRepository extends ReactiveCrudRepository<RefreshToken, String> {

    /**
     * Busca todos los refresh tokens de un usuario.
     *
     * Útil para:
     * - Listar sesiones activas del usuario
     * - Invalidar todas las sesiones en caso de seguridad
     *
     * @param userId ID del usuario
     * @return Flux de refresh tokens del usuario
     */
    Flux<RefreshToken> findByUserId(UUID userId);

    /**
     * Busca refresh tokens por email.
     *
     * @param email Email del usuario
     * @return Flux de refresh tokens
     */
    Flux<RefreshToken> findByEmail(String email);

    /**
     * Elimina todos los refresh tokens de un usuario.
     *
     * Útil para:
     * - Logout de todas las sesiones
     * - Cierre forzado por seguridad
     * - Cambio de contraseña
     *
     * @param userId ID del usuario
     * @return Mono que completa cuando se eliminan
     */
    Mono<Void> deleteByUserId(UUID userId);

    /**
     * Cuenta cuántos refresh tokens activos tiene un usuario.
     *
     * Útil para:
     * - Limitar número de sesiones simultáneas
     * - Auditoría de seguridad
     *
     * @param userId ID del usuario
     * @return Mono con el número de tokens activos
     */
    Mono<Long> countByUserId(UUID userId);
}
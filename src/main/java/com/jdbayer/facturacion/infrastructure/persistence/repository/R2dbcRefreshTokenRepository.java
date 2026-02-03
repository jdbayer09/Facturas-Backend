package com.jdbayer.facturacion.infrastructure.persistence.repository;

import com.jdbayer.facturacion.infrastructure.persistence.entity.RefreshTokenEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Repositorio Spring Data R2DBC para RefreshToken.
 *
 * Maneja los refresh tokens para renovación de sesión usando PostgreSQL.
 */
@Repository
public interface R2dbcRefreshTokenRepository extends R2dbcRepository<RefreshTokenEntity, String> {

    /**
     * Busca todos los refresh tokens de un usuario.
     *
     * @param userId ID del usuario
     * @return Flux de refresh tokens
     */
    @Query("SELECT * FROM security.refresh_tokens WHERE user_id = :userId")
    Flux<RefreshTokenEntity> findByUserId(UUID userId);

    /**
     * Busca refresh tokens por email.
     *
     * @param email Email del usuario
     * @return Flux de refresh tokens
     */
    @Query("SELECT * FROM security.refresh_tokens WHERE email = :email")
    Flux<RefreshTokenEntity> findByEmail(String email);

    /**
     * Elimina todos los refresh tokens de un usuario.
     *
     * @param userId ID del usuario
     * @return Mono con el número de registros eliminados
     */
    @Modifying
    @Query("DELETE FROM security.refresh_tokens WHERE user_id = :userId")
    Mono<Integer> deleteByUserId(UUID userId);

    /**
     * Cuenta cuántos refresh tokens activos tiene un usuario.
     *
     * @param userId ID del usuario
     * @return Mono con el número de tokens activos
     */
    @Query("SELECT COUNT(*) FROM security.refresh_tokens WHERE user_id = :userId AND expires_at > :now")
    Mono<Long> countActiveByUserId(UUID userId, Instant now);

    /**
     * Elimina tokens expirados.
     *
     * Se puede ejecutar con un scheduler periódico.
     *
     * @param now Fecha y hora actual
     * @return Mono con el número de tokens eliminados
     */
    @Modifying
    @Query("DELETE FROM security.refresh_tokens WHERE expires_at < :now")
    Mono<Integer> deleteExpiredTokens(Instant now);

    /**
     * Busca un token específico de un usuario.
     *
     * @param token Token a buscar
     * @param userId ID del usuario
     * @return Mono con el token encontrado
     */
    @Query("SELECT * FROM security.refresh_tokens WHERE token = :token AND user_id = :userId")
    Mono<RefreshTokenEntity> findByTokenAndUserId(String token, UUID userId);

    /**
     * Marca un token como usado.
     *
     * @param token Token a marcar
     * @param lastUsedAt Fecha de último uso
     * @return Mono con el número de filas actualizadas
     */
    @Modifying
    @Query("UPDATE security.refresh_tokens SET used = true, last_used_at = :lastUsedAt WHERE token = :token")
    Mono<Integer> markAsUsed(String token, Instant lastUsedAt);
}
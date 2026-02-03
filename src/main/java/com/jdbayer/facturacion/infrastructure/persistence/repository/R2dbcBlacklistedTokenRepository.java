package com.jdbayer.facturacion.infrastructure.persistence.repository;

import com.jdbayer.facturacion.infrastructure.persistence.entity.BlacklistedTokenEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Repositorio Spring Data R2DBC para BlacklistedToken.
 *
 * Maneja la blacklist de tokens invalidados (logout) usando PostgreSQL.
 */
@Repository
public interface R2dbcBlacklistedTokenRepository extends R2dbcRepository<BlacklistedTokenEntity, String> {

    /**
     * Busca todos los tokens blacklisted de un usuario.
     *
     * Útil para auditoría y análisis de seguridad.
     *
     * @param userId ID del usuario
     * @return Flux de tokens blacklisted
     */
    @Query("SELECT * FROM security.blacklisted_tokens WHERE user_id = :userId")
    Flux<BlacklistedTokenEntity> findByUserId(UUID userId);

    /**
     * Elimina todos los tokens blacklisted de un usuario.
     *
     * Se usa cuando todos los tokens ya expiraron naturalmente.
     *
     * @param userId ID del usuario
     * @return Mono con el número de registros eliminados
     */
    @Modifying
    @Query("DELETE FROM security.blacklisted_tokens WHERE user_id = :userId")
    Mono<Integer> deleteByUserId(UUID userId);

    /**
     * Verifica si un token está en la blacklist.
     *
     * Este método es crítico para validación de tokens.
     *
     * @param token Token JWT a verificar
     * @return Mono<Boolean> - true si el token está blacklisted
     */
    @Query("SELECT EXISTS(SELECT 1 FROM security.blacklisted_tokens WHERE token = :token)")
    Mono<Boolean> existsByToken(String token);

    /**
     * Elimina tokens expirados de la blacklist.
     *
     * Se puede ejecutar con un scheduler periódico para limpiar la BD.
     *
     * @param now Fecha y hora actual
     * @return Mono con el número de tokens eliminados
     */
    @Modifying
    @Query("DELETE FROM security.blacklisted_tokens WHERE expires_at < :now")
    Mono<Integer> deleteExpiredTokens(Instant now);

    /**
     * Cuenta tokens blacklisted por un usuario.
     *
     * @param userId ID del usuario
     * @return Mono con el número de tokens
     */
    @Query("SELECT COUNT(*) FROM security.blacklisted_tokens WHERE user_id = :userId")
    Mono<Long> countByUserId(UUID userId);

    /**
     * Busca tokens blacklisted por razón.
     *
     * Útil para análisis de seguridad.
     *
     * @param reason Razón de blacklist
     * @return Flux de tokens
     */
    @Query("SELECT * FROM security.blacklisted_tokens WHERE reason = :reason")
    Flux<BlacklistedTokenEntity> findByReason(String reason);
}
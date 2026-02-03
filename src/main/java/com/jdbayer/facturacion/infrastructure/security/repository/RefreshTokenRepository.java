package com.jdbayer.facturacion.infrastructure.security.repository;

import com.jdbayer.facturacion.infrastructure.security.token.RefreshToken;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

/**
 * Repositorio REACTIVO para RefreshToken usando ReactiveRedisTemplate.
 *
 * Spring Data Redis no soporta ReactiveCrudRepository,
 * así que usamos ReactiveRedisTemplate directamente.
 *
 * Maneja los refresh tokens para renovación de sesión.
 */
@Repository
public class RefreshTokenRepository {

    private static final String KEY_PREFIX = "refresh_token:";
    private static final String USER_INDEX_PREFIX = "refresh_tokens_by_user:";
    private static final String EMAIL_INDEX_PREFIX = "refresh_tokens_by_email:";

    private final ReactiveRedisTemplate<String, RefreshToken> tokenTemplate;
    private final ReactiveRedisTemplate<String, String> stringTemplate;

    public RefreshTokenRepository(
            ReactiveRedisTemplate<String, RefreshToken> tokenTemplate,
            ReactiveRedisTemplate<String, String> stringTemplate
    ) {
        this.tokenTemplate = tokenTemplate;
        this.stringTemplate = stringTemplate;
    }

    /**
     * Guarda un refresh token con TTL.
     *
     * @param token Token a guardar
     * @return Mono con el token guardado
     */
    public Mono<RefreshToken> save(RefreshToken token) {
        String key = KEY_PREFIX + token.getToken();
        Duration ttl = Duration.ofSeconds(token.getTtl());

        return redisTemplate.opsForValue()
                .set(key, token, ttl)
                .flatMap(success -> {
                    if (success) {
                        // Agregar a índices para búsquedas
                        String userIndexKey = USER_INDEX_PREFIX + token.getUserId();
                        String emailIndexKey = EMAIL_INDEX_PREFIX + token.getEmail();

                        return redisTemplate.opsForSet()
                                .add(userIndexKey, token.getToken())
                                .then(redisTemplate.expire(userIndexKey, ttl))
                                .then(redisTemplate.opsForSet().add(emailIndexKey, token.getToken()))
                                .then(redisTemplate.expire(emailIndexKey, ttl))
                                .thenReturn(token);
                    }
                    return Mono.error(new RuntimeException("Failed to save refresh token"));
                });
    }

    /**
     * Busca un token por su ID.
     *
     * @param tokenId ID del token (el token JWT mismo)
     * @return Mono con el token o vacío
     */
    public Mono<RefreshToken> findById(String tokenId) {
        String key = KEY_PREFIX + tokenId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Verifica si un token existe.
     *
     * @param tokenId ID del token
     * @return Mono<Boolean> - true si existe
     */
    public Mono<Boolean> existsById(String tokenId) {
        String key = KEY_PREFIX + tokenId;
        return redisTemplate.hasKey(key);
    }

    /**
     * Elimina un token.
     *
     * @param tokenId ID del token
     * @return Mono que completa cuando se elimina
     */
    public Mono<Void> deleteById(String tokenId) {
        // Primero buscar el token para obtener los índices
        return findById(tokenId)
                .flatMap(token -> {
                    String key = KEY_PREFIX + tokenId;
                    String userIndexKey = USER_INDEX_PREFIX + token.getUserId();
                    String emailIndexKey = EMAIL_INDEX_PREFIX + token.getEmail();

                    return redisTemplate.delete(key)
                            .then(redisTemplate.opsForSet().remove(userIndexKey, tokenId))
                            .then(redisTemplate.opsForSet().remove(emailIndexKey, tokenId))
                            .then();
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // Si no existe, simplemente completar
                    return Mono.empty();
                }));
    }

    /**
     * Busca todos los refresh tokens de un usuario.
     *
     * @param userId ID del usuario
     * @return Flux de refresh tokens
     */
    public Flux<RefreshToken> findByUserId(UUID userId) {
        String userIndexKey = USER_INDEX_PREFIX + userId;

        return redisTemplate.opsForSet().members(userIndexKey)
                .flatMap(tokenId -> findById(tokenId))
                .filter(token -> token != null);
    }

    /**
     * Busca refresh tokens por email.
     *
     * @param email Email del usuario
     * @return Flux de refresh tokens
     */
    public Flux<RefreshToken> findByEmail(String email) {
        String emailIndexKey = EMAIL_INDEX_PREFIX + email;

        return redisTemplate.opsForSet().members(emailIndexKey)
                .flatMap(tokenId -> findById(tokenId))
                .filter(token -> token != null);
    }

    /**
     * Elimina todos los refresh tokens de un usuario.
     *
     * @param userId ID del usuario
     * @return Mono que completa cuando se eliminan
     */
    public Mono<Void> deleteByUserId(UUID userId) {
        String userIndexKey = USER_INDEX_PREFIX + userId;

        return redisTemplate.opsForSet().members(userIndexKey)
                .flatMap(this::deleteById)
                .then(redisTemplate.delete(userIndexKey))
                .then();
    }

    /**
     * Cuenta cuántos refresh tokens activos tiene un usuario.
     *
     * @param userId ID del usuario
     * @return Mono con el número de tokens activos
     */
    public Mono<Long> countByUserId(UUID userId) {
        String userIndexKey = USER_INDEX_PREFIX + userId;
        return redisTemplate.opsForSet().size(userIndexKey);
    }
}
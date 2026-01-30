package com.jdbayer.facturacion.infrastructure.security.repository;

import com.jdbayer.facturacion.infrastructure.security.token.BlacklistedToken;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repositorio REACTIVO Spring Data Redis para BlacklistedToken.
 *
 * IMPORTANTE: Usa ReactiveCrudRepository para retornar Mono/Flux
 * en lugar de Optional/List, coherente con WebFlux.
 *
 * Maneja la blacklist de tokens invalidados (logout).
 */
@Repository
public interface BlacklistedTokenRepository extends ReactiveCrudRepository<BlacklistedToken, String> {

    /**
     * Busca todos los tokens blacklisted de un usuario.
     *
     * Útil para auditoría y análisis de seguridad.
     *
     * @param userId ID del usuario
     * @return Flux de tokens blacklisted
     */
    Flux<BlacklistedToken> findByUserId(UUID userId);

    /**
     * Elimina todos los tokens blacklisted de un usuario.
     *
     * Se usa cuando todos los tokens ya expiraron naturalmente.
     *
     * @param userId ID del usuario
     * @return Mono que completa cuando se eliminan
     */
    Mono<Void> deleteByUserId(UUID userId);

    /**
     * Verifica si un token está en la blacklist.
     *
     * Este método es crítico para validación de tokens.
     *
     * @param token Token JWT a verificar
     * @return Mono<Boolean> - true si el token está blacklisted
     */
    Mono<Boolean> existsById(String token);
}
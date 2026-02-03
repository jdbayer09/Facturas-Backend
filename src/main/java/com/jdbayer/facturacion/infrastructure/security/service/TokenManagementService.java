package com.jdbayer.facturacion.infrastructure.security.service;

import com.jdbayer.facturacion.domain.model.User;
import com.jdbayer.facturacion.infrastructure.persistence.entity.BlacklistedTokenEntity;
import com.jdbayer.facturacion.infrastructure.persistence.entity.RefreshTokenEntity;
import com.jdbayer.facturacion.infrastructure.persistence.repository.R2dbcBlacklistedTokenRepository;
import com.jdbayer.facturacion.infrastructure.persistence.repository.R2dbcRefreshTokenRepository;
import com.jdbayer.facturacion.infrastructure.security.jwt.JwtProperties;
import com.jdbayer.facturacion.infrastructure.security.jwt.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Servicio para gestión de tokens JWT usando PostgreSQL.
 *
 * Migrado de Redis a PostgreSQL para simplificar la infraestructura.
 *
 * Responsabilidades:
 * - Crear y validar refresh tokens
 * - Manejar la blacklist de tokens (logout)
 * - Gestionar sesiones de usuarios
 * - Proporcionar métodos reactivos para operaciones con tokens
 */
@Service
@Slf4j
public class TokenManagementService {

    private final R2dbcRefreshTokenRepository refreshTokenRepository;
    private final R2dbcBlacklistedTokenRepository blacklistedTokenRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public TokenManagementService(
            R2dbcRefreshTokenRepository refreshTokenRepository,
            R2dbcBlacklistedTokenRepository blacklistedTokenRepository,
            JwtService jwtService,
            JwtProperties jwtProperties
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    /**
     * Crea y almacena un nuevo refresh token en PostgreSQL.
     *
     * @param user Usuario para el cual crear el token
     * @param ipAddress IP del cliente
     * @param userAgent User agent del cliente
     * @return Mono con el refresh token JWT
     */
    public Mono<String> createRefreshToken(User user, String ipAddress, String userAgent) {
        log.debug("Creando refresh token para usuario: {}", user.getEmail().value());

        // Generar el refresh token JWT
        String token = jwtService.generateRefreshToken(user);

        // Calcular fecha de expiración
        Instant expiresAt = Instant.now()
                .plusMillis(jwtProperties.getRefreshExpiration());

        // Crear entidad RefreshToken
        RefreshTokenEntity refreshToken = new RefreshTokenEntity(
                token,
                user.getId(),
                user.getEmail().value(),
                expiresAt,
                ipAddress,
                userAgent
        );

        // Guardar en PostgreSQL
        return refreshTokenRepository.save(refreshToken)
                .doOnSuccess(saved ->
                        log.info("Refresh token creado para usuario: {}", user.getEmail().value())
                )
                .map(RefreshTokenEntity::getToken);
    }

    /**
     * Valida un refresh token y lo marca como usado.
     *
     * @param token Refresh token a validar
     * @return Mono con el refresh token si es válido, error si no
     */
    public Mono<RefreshTokenEntity> validateAndUseRefreshToken(String token) {
        log.debug("Validando refresh token");

        // Buscar el token en PostgreSQL
        return refreshTokenRepository.findById(token)
                .switchIfEmpty(Mono.error(
                        new IllegalArgumentException("Refresh token inválido o expirado")
                ))
                .flatMap(refreshToken -> {
                    // Verificar que no esté expirado
                    if (refreshToken.isExpired()) {
                        log.warn("Refresh token expirado para usuario: {}", refreshToken.getEmail());
                        return refreshTokenRepository.deleteById(token)
                                .then(Mono.error(
                                        new IllegalArgumentException("Refresh token expirado")
                                ));
                    }

                    // Verificar que no haya sido usado (detectar reutilización)
                    if (Boolean.TRUE.equals(refreshToken.getUsed())) {
                        log.error("Intento de reutilización de refresh token para usuario: {}",
                                refreshToken.getEmail());
                        // Invalidar TODOS los refresh tokens del usuario por seguridad
                        return invalidateAllUserTokens(refreshToken.getUserId(), "token_reuse_detected")
                                .then(Mono.error(
                                        new SecurityException("Refresh token ya utilizado")
                                ));
                    }

                    // Validar el token JWT
                    if (!jwtService.validateToken(token)) {
                        log.warn("Refresh token JWT inválido");
                        return refreshTokenRepository.deleteById(token)
                                .then(Mono.error(
                                        new IllegalArgumentException("Refresh token inválido")
                                ));
                    }

                    // Marcar como usado
                    return refreshTokenRepository.markAsUsed(token, Instant.now())
                            .then(refreshTokenRepository.findById(token))
                            .doOnSuccess(saved ->
                                    log.info("Refresh token validado y usado para usuario: {}",
                                            saved.getEmail())
                            );
                });
    }

    /**
     * Invalida un access token (logout).
     *
     * @param token Access token a invalidar
     * @param userId ID del usuario
     * @param email Email del usuario
     * @param reason Razón de invalidación
     * @param ipAddress IP del cliente
     */
    public Mono<Void> blacklistToken(
            String token,
            UUID userId,
            String email,
            String reason,
            String ipAddress
    ) {
        log.debug("Agregando token a blacklist para usuario: {}", email);

        // Calcular fecha de expiración del token
        Instant expiresAt = calculateTokenExpiration(token);

        // Crear entrada en blacklist
        BlacklistedTokenEntity blacklistedToken = new BlacklistedTokenEntity(
                token,
                userId,
                email,
                reason,
                ipAddress,
                expiresAt
        );

        // Guardar en PostgreSQL
        return blacklistedTokenRepository.save(blacklistedToken)
                .doOnSuccess(saved ->
                        log.info("Token blacklisted para usuario: {} - Razón: {}", email, reason)
                )
                .then();
    }

    /**
     * Verifica si un token está en la blacklist.
     *
     * @param token Token a verificar
     * @return Mono<Boolean> - true si está blacklisted
     */
    public Mono<Boolean> isTokenBlacklisted(String token) {
        return blacklistedTokenRepository.existsByToken(token);
    }

    /**
     * Invalida todos los tokens (access y refresh) de un usuario.
     *
     * Útil para:
     * - Logout de todas las sesiones
     * - Cambio de contraseña
     * - Compromiso de seguridad
     *
     * @param userId ID del usuario
     * @param reason Razón de invalidación
     */
    public Mono<Void> invalidateAllUserTokens(UUID userId, String reason) {
        log.warn("Invalidando TODOS los tokens del usuario: {} - Razón: {}", userId, reason);

        // Eliminar todos los refresh tokens
        return refreshTokenRepository.deleteByUserId(userId)
                .doOnSuccess(count ->
                        log.info("Eliminados {} refresh tokens para usuario: {}", count, userId)
                )
                .then();
    }

    /**
     * Obtiene todas las sesiones activas de un usuario.
     *
     * @param userId ID del usuario
     * @return Flux de refresh tokens activos
     */
    public Flux<RefreshTokenEntity> getUserActiveSessions(UUID userId) {
        return refreshTokenRepository.findByUserId(userId)
                .filter(token -> !token.isExpired());
    }

    /**
     * Cuenta el número de sesiones activas de un usuario.
     *
     * @param userId ID del usuario
     * @return Mono con el número de sesiones activas
     */
    public Mono<Long> countUserActiveSessions(UUID userId) {
        return refreshTokenRepository.countActiveByUserId(userId, Instant.now());
    }

    /**
     * Elimina un refresh token específico (logout de una sesión).
     *
     * @param token Refresh token a eliminar
     */
    public Mono<Void> revokeRefreshToken(String token) {
        log.debug("Revocando refresh token");
        return refreshTokenRepository.deleteById(token);
    }

    /**
     * Calcula la fecha de expiración de un token JWT.
     */
    private Instant calculateTokenExpiration(String token) {
        try {
            return Instant.ofEpochMilli(
                    jwtService.extractAllClaims(token).getExpiration().getTime()
            );
        } catch (Exception e) {
            log.error("Error al calcular expiración del token: {}", e.getMessage());
            // Si no podemos calcular, usar el tiempo máximo de expiración
            return Instant.now().plusMillis(jwtProperties.getExpiration());
        }
    }

    /**
     * Limpia refresh tokens expirados.
     * Este método puede ser llamado por un scheduler periódico.
     */
    public Mono<Integer> cleanupExpiredRefreshTokens() {
        log.info("Limpiando refresh tokens expirados...");
        return refreshTokenRepository.deleteExpiredTokens(Instant.now())
                .doOnSuccess(count ->
                        log.info("Eliminados {} refresh tokens expirados", count)
                );
    }

    /**
     * Limpia tokens expirados de la blacklist.
     * Este método puede ser llamado por un scheduler periódico.
     */
    public Mono<Integer> cleanupExpiredBlacklistedTokens() {
        log.info("Limpiando tokens expirados de la blacklist...");
        return blacklistedTokenRepository.deleteExpiredTokens(Instant.now())
                .doOnSuccess(count ->
                        log.info("Eliminados {} tokens expirados de la blacklist", count)
                );
    }
}
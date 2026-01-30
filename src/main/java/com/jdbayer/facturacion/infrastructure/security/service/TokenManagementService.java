package com.jdbayer.facturacion.infrastructure.security.service;

import com.jdbayer.facturacion.domain.model.User;
import com.jdbayer.facturacion.infrastructure.security.jwt.JwtProperties;
import com.jdbayer.facturacion.infrastructure.security.jwt.JwtService;
import com.jdbayer.facturacion.infrastructure.security.repository.BlacklistedTokenRepository;
import com.jdbayer.facturacion.infrastructure.security.repository.RefreshTokenRepository;
import com.jdbayer.facturacion.infrastructure.security.token.BlacklistedToken;
import com.jdbayer.facturacion.infrastructure.security.token.RefreshToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Servicio para gestión de tokens JWT.
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

    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public TokenManagementService(
            RefreshTokenRepository refreshTokenRepository,
            BlacklistedTokenRepository blacklistedTokenRepository,
            JwtService jwtService,
            JwtProperties jwtProperties
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    /**
     * Crea y almacena un nuevo refresh token en Redis.
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

        // Crear entidad RefreshToken para Redis
        RefreshToken refreshToken = new RefreshToken(
                token,
                user.getId(),
                user.getEmail().value(),
                expiresAt,
                ipAddress,
                userAgent
        );

        // Guardar en Redis (ahora reactivo)
        return refreshTokenRepository.save(refreshToken)
                .doOnSuccess(saved ->
                        log.info("Refresh token creado para usuario: {}", user.getEmail().value())
                )
                .map(RefreshToken::getToken);
    }

    /**
     * Valida un refresh token y lo marca como usado.
     *
     * @param token Refresh token a validar
     * @return Mono con el refresh token si es válido, error si no
     */
    public Mono<RefreshToken> validateAndUseRefreshToken(String token) {
        log.debug("Validando refresh token");

        // Buscar el token en Redis (ahora es reactivo)
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
                    if (refreshToken.isUsed()) {
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

                    // Marcar como usado y guardar
                    refreshToken.markAsUsed();
                    return refreshTokenRepository.save(refreshToken)
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

        // Calcular TTL basado en la expiración del token
        long ttl = calculateTokenTTL(token);

        // Crear entrada en blacklist
        BlacklistedToken blacklistedToken = new BlacklistedToken(
                token,
                userId,
                email,
                reason,
                ipAddress,
                ttl
        );

        // Guardar en Redis (ahora reactivo)
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
        return blacklistedTokenRepository.existsById(token);
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

        // Eliminar todos los refresh tokens (ahora reactivo)
        return refreshTokenRepository.deleteByUserId(userId)
                .doOnSuccess(v ->
                        log.info("Todos los tokens invalidados para usuario: {}", userId)
                );
    }

    /**
     * Obtiene todas las sesiones activas de un usuario.
     *
     * @param userId ID del usuario
     * @return Flux de refresh tokens activos
     */
    public Flux<RefreshToken> getUserActiveSessions(UUID userId) {
        return refreshTokenRepository.findByUserId(userId);
    }

    /**
     * Cuenta el número de sesiones activas de un usuario.
     *
     * @param userId ID del usuario
     * @return Mono con el número de sesiones activas
     */
    public Mono<Long> countUserActiveSessions(UUID userId) {
        return refreshTokenRepository.countByUserId(userId);
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
     * Calcula el TTL restante de un token JWT en segundos.
     */
    private long calculateTokenTTL(String token) {
        try {
            Instant expiration = Instant.ofEpochMilli(
                    jwtService.extractAllClaims(token).getExpiration().getTime()
            );
            long ttl = expiration.getEpochSecond() - Instant.now().getEpochSecond();
            return Math.max(ttl, 0);
        } catch (Exception e) {
            log.error("Error al calcular TTL del token: {}", e.getMessage());
            // Si no podemos calcular, usar el tiempo máximo de expiración
            return jwtProperties.getExpiration() / 1000;
        }
    }

    /**
     * Limpia refresh tokens expirados.
     * Este método puede ser llamado por un scheduler periódico.
     */
    public Mono<Void> cleanupExpiredTokens() {
        return Mono.fromRunnable(() -> {
            log.info("Limpiando refresh tokens expirados...");
            // Redis hace esto automáticamente con TTL, pero podemos agregar
            // lógica adicional aquí si es necesario
        });
    }
}
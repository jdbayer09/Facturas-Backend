package com.jdbayer.facturacion.infrastructure.security.token;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad para almacenar Refresh Tokens en Redis.
 *
 * Los refresh tokens permiten obtener nuevos access tokens sin
 * volver a autenticarse con usuario/contraseña.
 *
 * Se almacenan en Redis porque:
 * - Son temporales (7 días por defecto)
 * - Necesitan acceso rápido
 * - No requieren persistencia permanente
 *
 * @RedisHash: Indica que esta entidad se almacena en Redis
 */
@Getter
@Setter
@NoArgsConstructor
@RedisHash(value = "refresh_token", timeToLive = 604800) // 7 días en segundos
public class RefreshToken {

    /**
     * ID único del refresh token (el token JWT mismo).
     */
    @Id
    private String token;

    /**
     * ID del usuario al que pertenece este token.
     * Indexed para poder buscar por userId.
     */
    @Indexed
    private UUID userId;

    /**
     * Email del usuario (para logging y debugging).
     */
    private String email;

    /**
     * Fecha de creación del token.
     */
    private Instant createdAt;

    /**
     * Fecha de expiración del token.
     */
    private Instant expiresAt;

    /**
     * IP desde la cual se creó el token.
     */
    private String ipAddress;

    /**
     * User Agent del cliente.
     */
    private String userAgent;

    /**
     * Indica si el token ha sido usado (para detectar reutilización).
     */
    private boolean used;

    /**
     * Fecha en que fue usado por última vez.
     */
    private Instant lastUsedAt;

    /**
     * TTL dinámico en segundos.
     * Redis eliminará automáticamente este token cuando expire.
     */
    @TimeToLive
    private Long ttl;

    public RefreshToken(
            String token,
            UUID userId,
            String email,
            Instant expiresAt,
            String ipAddress,
            String userAgent
    ) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.createdAt = Instant.now();
        this.expiresAt = expiresAt;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.used = false;

        // Calcular TTL en segundos
        this.ttl = expiresAt.getEpochSecond() - Instant.now().getEpochSecond();
    }

    /**
     * Marca este token como usado.
     */
    public void markAsUsed() {
        this.used = true;
        this.lastUsedAt = Instant.now();
    }

    /**
     * Verifica si este token está expirado.
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
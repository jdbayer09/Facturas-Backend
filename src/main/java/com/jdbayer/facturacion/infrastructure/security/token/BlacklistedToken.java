package com.jdbayer.facturacion.infrastructure.security.token;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad para la blacklist de tokens JWT.
 *
 * Cuando un usuario hace logout, su token se agrega a esta blacklist
 * para prevenir su uso hasta que expire naturalmente.
 *
 * Se almacena en Redis porque:
 * - Es temporal (solo hasta que el token expire)
 * - Necesita acceso muy rápido (se verifica en cada request)
 * - No requiere persistencia permanente
 *
 * La blacklist se limpia automáticamente cuando los tokens expiran
 * gracias al TTL de Redis.
 *
 * NOTA: No usa @RedisHash porque usamos ReactiveRedisTemplate directamente.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistedToken {

    /**
     * ID único del token (el token JWT mismo).
     */
    private String token;

    /**
     * ID del usuario que hizo logout.
     */
    private UUID userId;

    /**
     * Email del usuario (para logging).
     */
    private String email;

    /**
     * Fecha y hora del logout.
     */
    private Instant blacklistedAt;

    /**
     * Razón de la invalidación.
     * Puede ser: "logout", "security_breach", "password_change", etc.
     */
    private String reason;

    /**
     * IP desde donde se hizo el logout.
     */
    private String ipAddress;

    /**
     * TTL dinámico en segundos.
     * Redis eliminará automáticamente este registro cuando el token expire.
     */
    private Long ttl;

    public BlacklistedToken(
            String token,
            UUID userId,
            String email,
            String reason,
            String ipAddress,
            Long ttl
    ) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.blacklistedAt = Instant.now();
        this.reason = reason;
        this.ipAddress = ipAddress;
        this.ttl = ttl;
    }
}
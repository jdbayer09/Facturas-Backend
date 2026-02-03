package com.jdbayer.facturacion.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidad para la blacklist de tokens JWT.
 *
 * Cuando un usuario hace logout, su token se agrega a esta blacklist
 * para prevenir su uso hasta que expire naturalmente.
 *
 * Migrado de Redis a PostgreSQL para simplificar la infraestructura.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "blacklisted_tokens", schema = "security")
public class BlacklistedTokenEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID único del token (el token JWT mismo).
     */
    @Id
    @Column("token")
    private String token;

    /**
     * ID del usuario que hizo logout.
     */
    @Column("user_id")
    private UUID userId;

    /**
     * Email del usuario (para logging).
     */
    @Column("email")
    private String email;

    /**
     * Fecha y hora del logout.
     */
    @Column("blacklisted_at")
    private Instant blacklistedAt;

    /**
     * Razón de la invalidación.
     * Puede ser: "logout", "security_breach", "password_change", etc.
     */
    @Column("reason")
    private String reason;

    /**
     * IP desde donde se hizo el logout.
     */
    @Column("ip_address")
    private String ipAddress;

    /**
     * Fecha de expiración del token.
     * Útil para limpiezas periódicas.
     */
    @Column("expires_at")
    private Instant expiresAt;

    /**
     * Constructor para crear un nuevo token blacklisted.
     */
    public BlacklistedTokenEntity(
            String token,
            UUID userId,
            String email,
            String reason,
            String ipAddress,
            Instant expiresAt
    ) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.blacklistedAt = Instant.now();
        this.reason = reason;
        this.ipAddress = ipAddress;
        this.expiresAt = expiresAt;
    }
}
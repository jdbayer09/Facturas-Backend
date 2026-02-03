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
 * Entidad para almacenar Refresh Tokens en PostgreSQL.
 *
 * Los refresh tokens permiten obtener nuevos access tokens sin
 * volver a autenticarse con usuario/contraseña.
 *
 * Migrado de Redis a PostgreSQL para simplificar la infraestructura.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "refresh_tokens", schema = "security")
public class RefreshTokenEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID único del refresh token (el token JWT mismo).
     */
    @Id
    @Column("token")
    private String token;

    /**
     * ID del usuario al que pertenece este token.
     */
    @Column("user_id")
    private UUID userId;

    /**
     * Email del usuario (para logging y debugging).
     */
    @Column("email")
    private String email;

    /**
     * Fecha de creación del token.
     */
    @Column("created_at")
    private Instant createdAt;

    /**
     * Fecha de expiración del token.
     */
    @Column("expires_at")
    private Instant expiresAt;

    /**
     * IP desde la cual se creó el token.
     */
    @Column("ip_address")
    private String ipAddress;

    /**
     * User Agent del cliente.
     */
    @Column("user_agent")
    private String userAgent;

    /**
     * Indica si el token ha sido usado (para detectar reutilización).
     */
    @Column("used")
    private Boolean used;

    /**
     * Fecha en que fue usado por última vez.
     */
    @Column("last_used_at")
    private Instant lastUsedAt;

    /**
     * Constructor para crear un nuevo refresh token.
     */
    public RefreshTokenEntity(
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
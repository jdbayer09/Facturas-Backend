package com.jdbayer.facturacion.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidad JPA para la tabla users.
 *
 * Implementa Persistable<UUID> para controlar explícitamente cuándo hacer INSERT vs UPDATE.
 * Esto es necesario porque estamos generando UUIDs en el dominio (no auto-generados por BD).
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "users", schema = "security")
public class UserEntity implements Serializable, Persistable<UUID> {

    @Serial
    private static final long serialVersionUID = -2698028677461799009L;

    @Id
    @Column("id")
    private UUID id;

    @Column("name")
    private String name;

    @Column("last_name")
    private String lastName;

    @Column("email")
    private String email;

    @Column("password")
    private String password;

    @Column("is_active")
    private Boolean active;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;

    /**
     * Retorna el ID de la entidad.
     * Requerido por Persistable.
     */
    @Override
    public UUID getId() {
        return id;
    }

    /**
     * Indica si la entidad es nueva (INSERT) o existente (UPDATE).
     *
     * Lógica:
     * - Si createdAt == updatedAt → es nueva (INSERT)
     * - Si createdAt != updatedAt → es existente (UPDATE)
     *
     * Esta estrategia funciona porque:
     * - Al crear: User.create() establece createdAt = updatedAt = Instant.now()
     * - Al actualizar: user.changeName(), changeEmail(), etc. actualizan solo updatedAt
     *
     * @return true si es nueva, false si ya existe en BD
     */
    @Override
    public boolean isNew() {
        // Si createdAt y updatedAt son iguales, es una nueva entidad
        return this.createdAt != null && this.createdAt.equals(this.updatedAt);
    }
}
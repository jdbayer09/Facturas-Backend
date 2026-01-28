package com.jdbayer.facturacion.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.aot.generate.Generated;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "users", schema = "security")
public class UserEntity implements Serializable {

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
}

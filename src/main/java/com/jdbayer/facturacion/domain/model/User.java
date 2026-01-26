package com.jdbayer.facturacion.domain.model;

import com.jdbayer.facturacion.domain.model.valueobject.Email;
import com.jdbayer.facturacion.domain.model.valueobject.Name;
import com.jdbayer.facturacion.domain.security.valueobject.PasswordHash;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class User {
    private final UUID id;
    private Name name;
    private Name lastName;
    private Email email;
    private final PasswordHash pass;
    private final Instant createdAt;

    public User(
            UUID id,
            Name name,
            Name lastName,
            Email email,
            PasswordHash pass,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.lastName = Objects.requireNonNull(lastName);
        this.email = Objects.requireNonNull(email);
        this.pass = Objects.requireNonNull(pass);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public void changeName(Name newName, Name newLastName) {
        this.name = newName;
        this.lastName = newLastName;
    }

    public void changeEmail(Email newEmail) {
        this.email = newEmail;
    }

}

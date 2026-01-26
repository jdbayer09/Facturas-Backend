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
    private final PasswordHash passwordHash;
    private final Instant createdAt;
    private Instant updatedAt;
    private boolean active;

    public User(
            UUID id,
            Name name,
            Name lastName,
            Email email,
            PasswordHash passwordHash,
            Instant createdAt,
            Instant updatedAt,
            boolean active
    ) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.lastName = Objects.requireNonNull(lastName);
        this.email = Objects.requireNonNull(email);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = updatedAt != null ? updatedAt : createdAt;
        this.active = active;
    }

    public static User create(
            Name name,
            Name lastName,
            Email email,
            PasswordHash passwordHash
    ) {
        return new User(
                UUID.randomUUID(),
                Objects.requireNonNull(name, "El nombre es obligatorio"),
                Objects.requireNonNull(lastName, "El apellido es obligatorio"),
                Objects.requireNonNull(email, "El email es obligatorio"),
                Objects.requireNonNull(passwordHash, "La contraseña es obligatoria"),
                Instant.now(),
                Instant.now(),
                true
        );
    }

    public void changeName(Name newName, Name newLastName) {
        Objects.requireNonNull(newName, "El nuevo nombre es obligatorio");
        Objects.requireNonNull(newLastName, "El nuevo apellido es obligatorio");
        this.name = newName;
        this.lastName = newLastName;
        this.updatedAt = Instant.now();
    }

    public void changeEmail(Email newEmail) {
        Objects.requireNonNull(newEmail, "El nuevo email es obligatorio");
        this.email = newEmail;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.active = true;
        this.updatedAt = Instant.now();
    }

    // IMPORTANTE: Para entidades, equals y hashCode basados en ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email=" + email.value() +
                ", active=" + active +
                '}';
    }
}
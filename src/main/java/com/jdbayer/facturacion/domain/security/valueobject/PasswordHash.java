package com.jdbayer.facturacion.domain.security.valueobject;

import java.util.Objects;

public final class PasswordHash {

    private final String value;

    private PasswordHash(String value) {
        Objects.requireNonNull(value, "El hash de contraseña no puede ser nulo");
        if (value.isBlank()) {
            throw new IllegalArgumentException("El hash de contraseña no puede estar vacío");
        }
        this.value = value;
    }

    public static PasswordHash fromHash(String hash) {
        return new PasswordHash(hash);
    }

    // IMPORTANTE: Debe ser PUBLIC
    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PasswordHash that = (PasswordHash) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}

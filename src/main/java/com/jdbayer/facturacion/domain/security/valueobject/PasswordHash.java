package com.jdbayer.facturacion.domain.security.valueobject;

import java.util.Objects;

public final class PasswordHash {

    private final String value;

    private PasswordHash(String value) {
        this.value = Objects.requireNonNull(value);
    }

    public static PasswordHash fromHash(String hash) {
        return new PasswordHash(hash);
    }

    String value() {
        return value;
    }
}

package com.jdbayer.facturacion.domain.model.valueobject;


import java.util.Objects;

public record Name(String value) {

    public Name(String value) {
        Objects.requireNonNull(value, "El nombre es obligatorio");
        String normalizado = value.trim().toUpperCase();
        if (normalizado.isBlank())
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        this.value = normalizado;
    }
}

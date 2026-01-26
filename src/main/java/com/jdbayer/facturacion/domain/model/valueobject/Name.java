package com.jdbayer.facturacion.domain.model.valueobject;


import com.jdbayer.facturacion.domain.exception.InvalidNameException;

import java.util.Objects;

public record Name(String value) {

    public Name(String value) {
        Objects.requireNonNull(value, "El nombre es obligatorio");
        String normalizado = value.trim().toUpperCase();
        if (normalizado.isBlank())
            throw new InvalidNameException("El nombre no puede estar vacío");
        if (normalizado.length() > 100)
            throw new InvalidNameException("El nombre no puede exceder 100 caracteres");
        this.value = normalizado;
    }
}

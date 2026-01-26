package com.jdbayer.facturacion.domain.model.valueobject;

import com.jdbayer.facturacion.domain.exception.InvalidEmailException;

import java.util.Objects;

public record Email(String value) {

    public Email(String value) {
        Objects.requireNonNull(value, "El email es obligatorio");
        String normalizado = value.trim().toLowerCase();
        if (normalizado.isBlank())
            throw new InvalidEmailException("El email no puede estar vacío");
        if (!normalizado.matches("^[^@]+@[^@]+\\.[^@]+$"))
            throw new InvalidEmailException("Email inválido");
        this.value = normalizado;
    }
}

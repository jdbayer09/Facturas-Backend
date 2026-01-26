package com.jdbayer.facturacion.domain.exception;

import java.io.Serial;
import java.util.UUID;

public class UserNotFoundException extends DomainException {
    @Serial
    private static final long serialVersionUID = -2648711230009560036L;

    public UserNotFoundException(UUID userId) {
        super("Usuario no encontrado con ID: " + userId);
    }

    public UserNotFoundException(String email) {
        super("Usuario no encontrado con email: " + email);
    }
}

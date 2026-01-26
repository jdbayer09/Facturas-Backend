package com.jdbayer.facturacion.domain.exception;

import java.io.Serial;

public class DuplicateEmailException extends DomainException {
    @Serial
    private static final long serialVersionUID = 7972885183088465662L;

    public DuplicateEmailException(String email) {
        super("Ya existe un usuario con el email: " + email);
    }
}

package com.jdbayer.facturacion.domain.exception;

import java.io.Serial;

public class InvalidCredentialsException extends DomainException {

    @Serial
    private static final long serialVersionUID = 2817651054741137164L;

    public InvalidCredentialsException() {
        super("Credenciales inválidas");
    }
}

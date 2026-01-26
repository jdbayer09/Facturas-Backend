package com.jdbayer.facturacion.domain.exception;

import java.io.Serial;

public class InvalidEmailException extends DomainException {

    @Serial
    private static final long serialVersionUID = 3984438721649953676L;

    public InvalidEmailException(String message) {
        super(message);
    }

    public InvalidEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}

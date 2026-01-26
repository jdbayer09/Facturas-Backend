package com.jdbayer.facturacion.domain.exception;

import java.io.Serial;

public class InvalidNameException extends DomainException{

    @Serial
    private static final long serialVersionUID = -5711336503650384595L;

    public InvalidNameException(String message) {
        super(message);
    }

    public InvalidNameException(String message, Throwable cause) {
        super(message, cause);
    }
}

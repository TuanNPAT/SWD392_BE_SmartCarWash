package com.swd392.smartcarwash.exception.exceptions;

public class AuthorizeException extends RuntimeException {
    public AuthorizeException(String message) {
        super(message);
    }

    public AuthorizeException(String message, Throwable cause) {
        super(message, cause);
    }
}

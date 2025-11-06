package org.lab.exception;

public class ForbiddenCustomException extends RuntimeException {

    public ForbiddenCustomException(String message) {
        super(message);
    }

    public ForbiddenCustomException(String message, Throwable cause) {
        super(message, cause);
    }
}

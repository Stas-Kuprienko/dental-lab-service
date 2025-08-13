package org.lab.exception;

public class ForbiddenCustomException extends RuntimeException {

    public ForbiddenCustomException(String message) {
        super(message);
    }
}

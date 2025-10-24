package org.lab.exception;

public class InternalCustomException extends RuntimeException {

    public InternalCustomException(String message) {
        super(message);
    }

    public InternalCustomException(Throwable cause) {
        super(cause);
    }
}

package org.lab.exception;

public class BadRequestCustomException extends RuntimeException {

    private static final String NOT_FOUND_BY_PASSED_ID = "Entity is not found by passed ID='{%s}'";

    public BadRequestCustomException(String message) {
        super(message);
    }

    public BadRequestCustomException(Throwable cause) {
        super(cause);
    }

    public static BadRequestCustomException notFoundPassedEntity(Object id) {
        return new BadRequestCustomException(NOT_FOUND_BY_PASSED_ID.formatted(id.toString()));
    }
}

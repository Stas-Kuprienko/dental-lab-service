package org.lab.exception;

public class BadRequestCustomException extends RuntimeException {

    private static final String ENTITY_TO_SAVE_HAS_ID = "Entity to save must not have ID (%s)";

    public BadRequestCustomException(String message) {
        super(message);
    }


    public static BadRequestCustomException savedEntityHasId(Object entity) {
        return new BadRequestCustomException(ENTITY_TO_SAVE_HAS_ID.formatted(entity.toString()));
    }
}

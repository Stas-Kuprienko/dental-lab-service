package org.lab.dental.exception;

public class PersistenceCustomException extends RuntimeException {

    public PersistenceCustomException(String message) {
        super(message);
    }


    public static PersistenceCustomException saveEntityWithId(Object entity) {
        return new PersistenceCustomException("Entity to save must not have ID (%s)".formatted(entity.toString()));
    }

    public static PersistenceCustomException updateEntityWithoutId(Object entity) {
        return new PersistenceCustomException("Entity to update must have ID (%s)".formatted(entity.toString()));
    }
}

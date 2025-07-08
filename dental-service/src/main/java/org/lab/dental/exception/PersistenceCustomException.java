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

    public static PersistenceCustomException findByNullableParam(String entity, String... params) {
        StringBuilder str = new StringBuilder();
        for (String param : params) {
            str.append(param)
                    .append(',')
                    .append(' ');
        }
        str.delete(str.length() - 2, str.length());
        return new PersistenceCustomException("Try to find entity (%s) with nullable params: '%s'".formatted(entity, str.toString()));
    }

    public static PersistenceCustomException nullParameters(String entity, String... params) {
        StringBuilder str = new StringBuilder();
        for (String param : params) {
            str.append(param)
                    .append(',')
                    .append(' ');
        }
        str.delete(str.length() - 2, str.length());
        return new PersistenceCustomException("Pass null parameters (%s) for entity: '%s'".formatted(str.toString(), entity));
    }
}

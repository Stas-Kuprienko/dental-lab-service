package org.lab.dental.exception;

import java.util.Map;

public class NotFoundCustomException extends RuntimeException {

    public NotFoundCustomException(String message) {
        super(message);
    }


    public static NotFoundCustomException byId(Class<?> entity, Object id) {
        return new NotFoundCustomException("Entity '%s' is not found by id = '%s'".formatted(entity, id));
    }

    public static NotFoundCustomException byParams(Class<?> entity, Map<String, Object> params) {
        StringBuilder str = new StringBuilder();
        params.forEach((key, value) -> str
                .append(key)
                .append('=')
                .append(value));
        return new NotFoundCustomException("Entity '%s' is not found by parameters: '%s'".formatted(entity, str.toString()));
    }
}

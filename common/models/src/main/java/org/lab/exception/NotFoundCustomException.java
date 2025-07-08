package org.lab.exception;

import java.util.Map;

public class NotFoundCustomException extends RuntimeException {

    public NotFoundCustomException(String message) {
        super(message);
    }


    public static NotFoundCustomException byId(String o, Object id) {
        return new NotFoundCustomException("Object '%s' is not found by id = '%s'".formatted(o, id));
    }

    public static NotFoundCustomException byParams(String o, Map<String, Object> params) {
        StringBuilder str = new StringBuilder();
        params.forEach((key, value) -> str
                .append(key)
                .append('=')
                .append(value));
        return new NotFoundCustomException("Object '%s' is not found by parameters: '%s'".formatted(o, str.toString()));
    }
}

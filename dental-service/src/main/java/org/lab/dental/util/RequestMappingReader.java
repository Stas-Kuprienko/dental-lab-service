package org.lab.dental.util;

import org.lab.exception.InternalCustomException;
import org.springframework.web.bind.annotation.RequestMapping;

public final class RequestMappingReader {

    private RequestMappingReader() {}


    public static String read(Class<?> controller) {
        RequestMapping requestMapping = controller.getAnnotation(RequestMapping.class);
        if (requestMapping == null) {
            throw new InternalCustomException("@RequestMapping is not found in class '%s'".formatted(controller.getName()));
        }
        if (requestMapping.value().length > 1) {
            throw new InternalCustomException("@RequestMapping contains more than one value in class '%s'".formatted(controller.getName()));
        }
        if (requestMapping.value().length == 0) {
            throw new InternalCustomException("@RequestMapping does not contain value in class '%s'".formatted(controller.getName()));
        }
        return requestMapping.value()[0];
    }
}

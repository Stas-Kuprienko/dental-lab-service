package org.lab.dental.util;

import org.lab.dental.exception.InternalServiceException;
import org.springframework.web.bind.annotation.RequestMapping;

public final class RequestMappingReader {

    private RequestMappingReader() {}


    public static String read(Class<?> controller) {
        RequestMapping requestMapping = controller.getAnnotation(RequestMapping.class);
        if (requestMapping == null) {
            throw new InternalServiceException("@RequestMapping is not found in class '%s'".formatted(controller.getName()));
        }
        if (requestMapping.value().length > 1) {
            throw new InternalServiceException("@RequestMapping contains more than one value in class '%s'".formatted(controller.getName()));
        }
        if (requestMapping.value().length == 0) {
            throw new InternalServiceException("@RequestMapping does not contain value in class '%s'".formatted(controller.getName()));
        }
        return requestMapping.value()[0];
    }
}

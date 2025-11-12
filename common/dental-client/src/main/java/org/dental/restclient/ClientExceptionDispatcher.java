package org.dental.restclient;

import org.lab.exception.BadRequestCustomException;
import org.lab.exception.ForbiddenCustomException;
import org.lab.exception.ApplicationCustomException;
import org.lab.exception.NotFoundCustomException;
import org.springframework.http.ResponseEntity;

public abstract class ClientExceptionDispatcher {


    protected <T> void check(ResponseEntity<T> response) {
        if (!response.getStatusCode().isError()) {
            return;
        }
        int code = response.getStatusCode().value();
        switch (code) {
            case 400 -> throw new BadRequestCustomException(getMessageIfExist(response));
            case 403, 401 -> throw new ForbiddenCustomException(getMessageIfExist(response));
            case 404 -> throw new NotFoundCustomException(getMessageIfExist(response));
            default -> throw new ApplicationCustomException(getMessageIfExist(response));
        }
    }

    private <T> String getMessageIfExist(ResponseEntity<T> response) {
        return response.hasBody() ? response.getBody().toString() : "error";
    }
}

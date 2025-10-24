package org.dental.restclient;

import org.lab.exception.BadRequestCustomException;
import org.lab.exception.ForbiddenCustomException;
import org.lab.exception.InternalCustomException;
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
            default -> throw new InternalCustomException(getMessageIfExist(response));
        }
    }

    protected <T> T getBodyOrThrowNotFoundEx(ResponseEntity<T> response, String message) {
        if (response.hasBody()) {
            return response.getBody();
        } else {
            throw new NotFoundCustomException(message);
        }
    }


    private <T> String getMessageIfExist(ResponseEntity<T> response) {
        return response.hasBody() ? response.getBody().toString() : "error";
    }
}

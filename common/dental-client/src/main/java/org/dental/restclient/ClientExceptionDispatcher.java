package org.dental.restclient;

import org.lab.exception.BadRequestCustomException;
import org.lab.exception.NotFoundCustomException;
import org.springframework.http.ResponseEntity;

public abstract class ClientExceptionDispatcher {


    protected <T> void check(ResponseEntity<T> response) {
        if (!response.getStatusCode().isError()) {
            return;
        }
        int code = response.getStatusCode().value();
        //TODO
        switch (code) {
            case 400 -> throw new BadRequestCustomException(getMessageIfExist(response));
            case 404 -> throw new NotFoundCustomException(getMessageIfExist(response));
            default -> throw new RuntimeException(getMessageIfExist(response));
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
        //TODO
        return response.hasBody() ? response.getBody().toString() : "error";
    }
}

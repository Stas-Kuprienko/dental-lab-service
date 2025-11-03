package org.lab.dental.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.lab.dental.exception.NotFoundCustomException;
import org.lab.dental.exception.PersistenceCustomException;
import org.lab.exception.BadRequestCustomException;
import org.lab.exception.ForbiddenCustomException;
import org.lab.exception.InternalCustomException;
import org.lab.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class MyExceptionHandler {


    @ExceptionHandler(BadRequestCustomException.class)
    public ResponseEntity<ErrorResponse> badRequestHandle(BadRequestCustomException e) {
        log.info(e.getMessage());
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        return ResponseEntity
                .status(httpStatus.value())
                .body(new ErrorResponse(httpStatus.name(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validationExceptionHandle(MethodArgumentNotValidException ex) {

        StringBuilder stringBuilder = new StringBuilder();
        List<FieldError> errors = ex.getBindingResult().getFieldErrors();

        for (int i = 0; i < errors.size(); i++) {
            FieldError error = errors.get(i);
            stringBuilder.append(error.getField()).append(" ").append(error.getDefaultMessage());
            if (i != errors.size() - 1) { stringBuilder.append(", "); }
        }
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        return ResponseEntity
                .status(httpStatus.value())
                .body(new ErrorResponse(httpStatus.name(), stringBuilder.toString()));
    }

    @ExceptionHandler(PersistenceCustomException.class)
    public ResponseEntity<ErrorResponse> persistenceExceptionHandle(PersistenceCustomException e) {
        log.info(e.getMessage());
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        return ResponseEntity
                .status(httpStatus.value())
                .body(new ErrorResponse(httpStatus.name(), e.getMessage()));
    }

    @ExceptionHandler(NotFoundCustomException.class)
    public ResponseEntity<ErrorResponse> notFoundHandle(NotFoundCustomException e) {
        log.info(e.getMessage());
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        return ResponseEntity
                .status(httpStatus.value())
                .body(new ErrorResponse(httpStatus.name(), e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> illegalArgumentHandle(IllegalArgumentException e) {
        log.warn(e.getMessage(), e);
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        return ResponseEntity
                .status(httpStatus.value())
                .body(new ErrorResponse(httpStatus.name(), e.getMessage()));
    }

    @ExceptionHandler(ForbiddenCustomException.class)
    public ResponseEntity<ErrorResponse> forbiddenHandle(ForbiddenCustomException e) {
        log.warn(e.getMessage(), e);
        HttpStatus httpStatus = HttpStatus.FORBIDDEN;
        return ResponseEntity
                .status(httpStatus.value())
                .body(new ErrorResponse(httpStatus.name(), e.getMessage()));
    }

    @ExceptionHandler(InternalCustomException.class)
    public ResponseEntity<ErrorResponse> internalCustomExceptionHandle(InternalCustomException e) {
        log.error(e.getMessage(), e);
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(httpStatus.value())
                .body(new ErrorResponse(httpStatus.name(), httpStatus.getReasonPhrase()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> exceptionHandle(Exception e) {
        log.error(e.getMessage(), e);
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(httpStatus.value())
                .body(new ErrorResponse(httpStatus.name(), httpStatus.getReasonPhrase()));
    }
}

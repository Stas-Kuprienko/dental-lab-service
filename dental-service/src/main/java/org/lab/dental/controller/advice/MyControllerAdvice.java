package org.lab.dental.controller.advice;

import jakarta.ws.rs.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.lab.dental.exception.InternalServiceException;
import org.lab.dental.exception.NotFoundCustomException;
import org.lab.dental.exception.PersistenceCustomException;
import org.lab.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class MyControllerAdvice {


    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> badRequestHandle(BadRequestException e) {
        log.info(e.getMessage());
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        return ResponseEntity
                .status(httpStatus.value())
                .body(new ErrorResponse(httpStatus.name(), e.getMessage()));
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
        log.warn(e.getMessage());
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        return ResponseEntity
                .status(httpStatus.value())
                .body(new ErrorResponse(httpStatus.name(), httpStatus.getReasonPhrase()));
    }

    @ExceptionHandler(InternalServiceException.class)
    public ResponseEntity<ErrorResponse> internalServiceExceptionHandle(InternalServiceException e) {
        log.error(e.getMessage());
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(httpStatus.value())
                .body(new ErrorResponse(httpStatus.name(), httpStatus.getReasonPhrase()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> exceptionHandle(Exception e) {
        log.error(e.getMessage());
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(httpStatus.value())
                .body(new ErrorResponse(httpStatus.name(), httpStatus.getReasonPhrase()));
    }
}

package org.lab.uimvc.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.lab.exception.BadRequestCustomException;
import org.lab.exception.InternalCustomException;
import org.lab.exception.NotFoundCustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.ModelAndView;
import java.util.Locale;

@Slf4j
@ControllerAdvice
public class MyExceptionHandler {

    private static final String ERROR_PAGE = "error";
    private static final Locale DEFAULT_LOCALE = Locale.of("RU"); //temporary default

    private final MessageSource messageSource;


    @Autowired
    public MyExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }


    @ExceptionHandler(HttpClientErrorException.class)
    public ModelAndView httpClientError(HttpClientErrorException exception) {
        switch (exception.getStatusCode()) {
            case HttpStatus.INTERNAL_SERVER_ERROR -> log.error(exception.getMessage(), exception);
            case HttpStatus.FORBIDDEN -> log.warn(exception.getMessage());
            case HttpStatus.CONFLICT -> {
                log.info(exception.getMessage());
                return conflictError(exception.getMessage());
            }
            default -> log.info(exception.getMessage());
        }
        HttpStatus httpStatus = HttpStatus.resolve(exception.getStatusCode().value());
        return errorPage(httpStatus);
    }

    @ExceptionHandler(NotFoundCustomException.class)
    public ModelAndView notFound(NotFoundCustomException exception) {
      log.info(exception.getMessage());
      HttpStatus httpStatus = HttpStatus.NOT_FOUND;
      return errorPage(httpStatus);
    }

    @ExceptionHandler(BadRequestCustomException.class)
    public ModelAndView badRequest(BadRequestCustomException exception) {
      log.info(exception.getMessage());
      HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
      return errorPage(httpStatus);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView illegalArgument(IllegalArgumentException exception) {
      log.warn(exception.getMessage());
      HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
      return errorPage(httpStatus);
    }

    @ExceptionHandler(InternalCustomException.class)
    public ModelAndView internal(InternalCustomException exception) {
      log.error(exception.getMessage(), exception);
      HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
      return errorPage(httpStatus);
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView globalException(Exception exception) {
      log.error(exception.getMessage(), exception);
      HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
      return errorPage(httpStatus);
    }


    private ModelAndView errorPage(HttpStatus httpStatus) {
        String message;
        try {
            message =  messageSource.getMessage(httpStatus.name(), null, DEFAULT_LOCALE);
        } catch (NoSuchMessageException e) {
            log.info(e.getMessage());
            message = messageSource.getMessage("DEFAULT", null, DEFAULT_LOCALE);
        }
        ErrorResponse error = ErrorResponse.builder()
                .code(String.valueOf(httpStatus.value()))
                .message(message)
                .build();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("error", error);
        modelAndView.setViewName(ERROR_PAGE);
        return modelAndView;
    }

    private ModelAndView conflictError(String exceptionMessage) {
        HttpStatus httpStatus = HttpStatus.CONFLICT;
        String email = exceptionMessage.split("\"")[1];
        String message =  messageSource.getMessage(httpStatus.name(), new Object[]{email}, DEFAULT_LOCALE);
        ErrorResponse error = ErrorResponse.builder()
                .code(String.valueOf(httpStatus.value()))
                .message(message)
                .build();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("error", error);
        modelAndView.setViewName(ERROR_PAGE);
        return modelAndView;
    }
}

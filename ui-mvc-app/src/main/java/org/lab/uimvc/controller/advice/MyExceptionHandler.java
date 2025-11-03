package org.lab.uimvc.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.lab.exception.BadRequestCustomException;
import org.lab.exception.InternalCustomException;
import org.lab.exception.NotFoundCustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
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


    @ExceptionHandler(NotFoundCustomException.class)
    public ModelAndView notFound(NotFoundCustomException exception, ModelAndView modelAndView) {
      log.info(exception.getMessage());
      HttpStatus httpStatus = HttpStatus.NOT_FOUND;
      return errorPage(modelAndView, httpStatus);
    }

    @ExceptionHandler(BadRequestCustomException.class)
    public ModelAndView badRequest(BadRequestCustomException exception, ModelAndView modelAndView) {
      log.info(exception.getMessage());
      HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
      return errorPage(modelAndView, httpStatus);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView illegalArgument(IllegalArgumentException exception, ModelAndView modelAndView) {
      log.warn(exception.getMessage());
      HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
      return errorPage(modelAndView, httpStatus);
    }

    @ExceptionHandler(InternalCustomException.class)
    public ModelAndView badRequest(InternalCustomException exception, ModelAndView modelAndView) {
      log.error(exception.getMessage(), exception);
      HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
      return errorPage(modelAndView, httpStatus);
    }


    private ModelAndView errorPage(ModelAndView modelAndView, HttpStatus httpStatus) {
        String message = messageSource.getMessage(httpStatus.name(), null, DEFAULT_LOCALE);
        ErrorResponse error = ErrorResponse.builder()
                .code(HttpStatus.NOT_FOUND.value())
                .message(message)
                .build();
        modelAndView.addObject("error", error);
        modelAndView.setViewName(ERROR_PAGE);
        return modelAndView;
    }
}

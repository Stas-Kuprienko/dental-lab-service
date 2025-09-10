package org.lab.telegram_bot.exception;

public class IncorrectInputException extends RuntimeException {


    public IncorrectInputException(String message) {
        super(message);
    }

    public IncorrectInputException(String message, Throwable cause) {
        super(message, cause);
    }
}

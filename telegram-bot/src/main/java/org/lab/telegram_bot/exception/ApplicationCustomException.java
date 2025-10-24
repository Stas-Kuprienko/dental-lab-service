package org.lab.telegram_bot.exception;

public class ApplicationCustomException extends RuntimeException {

    public ApplicationCustomException(String message) {
        super(message);
    }

    public ApplicationCustomException(Throwable cause) {
        super(cause);
    }
}

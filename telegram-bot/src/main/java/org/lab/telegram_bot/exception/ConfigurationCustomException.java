package org.lab.telegram_bot.exception;

public class ConfigurationCustomException extends RuntimeException {

    public ConfigurationCustomException(String message) {
        super(message);
    }

    public ConfigurationCustomException(Throwable cause) {
        super(cause);
    }
}

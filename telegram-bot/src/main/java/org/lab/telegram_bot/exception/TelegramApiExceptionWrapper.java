package org.lab.telegram_bot.exception;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class TelegramApiExceptionWrapper extends RuntimeException {

    public TelegramApiExceptionWrapper(TelegramApiException cause) {
        super(cause);
    }
}

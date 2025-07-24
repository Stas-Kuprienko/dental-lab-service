package org.lab.telegram_bot.exception;

public class UnregisteredUserException extends RuntimeException {

    private final Long chatId;

    public UnregisteredUserException(Long chatId) {
        super();
        this.chatId = chatId;
    }

    public Long getChatId() {
        return chatId;
    }
}

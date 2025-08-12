package org.lab.telegram_bot.service;

import org.dental.restclient.TelegramChatService;
import org.lab.model.TelegramChat;
import org.lab.request.NewTelegramOtpLink;
import org.springframework.http.HttpHeaders;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class TelegramChatServiceWrapper {

    private final TelegramChatService telegramChatService;
    private final Consumer<HttpHeaders> httpHeadersConsumer;


    TelegramChatServiceWrapper(TelegramChatService telegramChatService, Consumer<HttpHeaders> httpHeadersConsumer) {
        this.telegramChatService = telegramChatService;
        this.httpHeadersConsumer = httpHeadersConsumer;
    }


    public void createLink(NewTelegramOtpLink newTelegramOtpLink) {
        telegramChatService.createLink(newTelegramOtpLink, httpHeadersConsumer);
    }

    public UUID bindTelegram(String key, String otp) {
        return telegramChatService.bindTelegram(key, otp, httpHeadersConsumer);
    }

    public Optional<TelegramChat> get(long chatId) {
        return telegramChatService.get(chatId, httpHeadersConsumer);
    }
}

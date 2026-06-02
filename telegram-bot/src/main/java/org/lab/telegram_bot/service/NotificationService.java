package org.lab.telegram_bot.service;

import lombok.extern.slf4j.Slf4j;
import org.lab.event.EventMessage;
import org.lab.telegram_bot.configuration.TelegramBotConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Consumer;

@Slf4j
@Service
public class NotificationService {

    private static final String WORKS_FOR_TOMORROW = "WORKS_FOR_TOMORROW";
    private static final String WORKS_FOR_TOMORROW_NULL = "WORKS_FOR_TOMORROW_NULL";

    private final MessageSource messageSource;
    private Consumer<BotApiMethod<?>> executor;


    @Autowired
    public NotificationService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }


    public void setExecutor(Consumer<BotApiMethod<?>> executor) {
        this.executor = executor;
    }

    public void sendMessageToChat(EventMessage message) {
        SendMessage sendMessage = buildSendMessage(message);
        executor.accept(sendMessage);
        log.info("Event {} is sent to ChatId={}", message.getId(), message.getChatId());
    }


    private SendMessage buildSendMessage(EventMessage message) {
        String text;
        Locale locale;
        if (message.getLanguage() == null) {
            locale = TelegramBotConfig.DEFAULT_LOCALE;
        } else {
            locale = Locale.of(message.getLanguage());
        }
        if (message.getText() == null || message.getText().isEmpty()) {
            text = messageSource.getMessage(WORKS_FOR_TOMORROW_NULL, null, locale);
        } else {
            String tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            text = messageSource.getMessage(WORKS_FOR_TOMORROW, new Object[]{tomorrow}, locale);
            text = text + '\n' + '\n' + message.getText();
        }
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText(text);
        return sendMessage;
    }
}

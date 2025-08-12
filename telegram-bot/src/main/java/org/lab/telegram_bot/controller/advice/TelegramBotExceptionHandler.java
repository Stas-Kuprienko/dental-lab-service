package org.lab.telegram_bot.controller.advice;

import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.lab.telegram_bot.exception.UnregisteredUserException;
import org.lab.telegram_bot.utils.ChatBotUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;

import static org.lab.telegram_bot.controller.advice.TelegramBotExceptionHandler.MessageTextKey.*;

@Slf4j
@Component
public class TelegramBotExceptionHandler {

    private final MessageSource messageSource;
    private final Map<String, BiFunction<Throwable, Update, SendMessage>> functionMap;

    @Autowired
    public TelegramBotExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
        functionMap = collectExceptionHandlers();
    }


    public SendMessage apply(Throwable throwable, Update update) {
        var method = functionMap.get(throwable.getClass().getSimpleName());
        if (method == null) {
            method = functionMap.get(null);
        }
        return method.apply(throwable, update);
    }

    public SendMessage unregisteredUserHandle(UnregisteredUserException e, Update update) {
        log.info(e.getMessage());
        Long chatId = e.getChatId();
        String username = ChatBotUtility.getUsername(update);
        Locale locale = ChatBotUtility.getLocale(update);

        String message = messageSource
                .getMessage(UNREGISTERED.name(), new Object[]{username, "Dental Mechanic Service"}, locale);
        //TODO url link button
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        return sendMessage;
    }

    public SendMessage notFoundHandle(NotFoundException e, Update update) {
        log.warn(e.getMessage());
        Long chatId = ChatBotUtility.getChatId(update);
        Locale locale = ChatBotUtility.getLocale(update);
        return buildMessage(chatId, locale, NOT_FOUND);
    }

    public SendMessage illegalArgumentHandle(IllegalArgumentException e, Update update) {
        log.warn(e.getMessage());
        Long chatId = ChatBotUtility.getChatId(update);
        Locale locale = ChatBotUtility.getLocale(update);
        return buildMessage(chatId, locale, ILLEGAL_ARGUMENT);
    }

    public SendMessage defaultHandle(Throwable e, Update update) {
        log.error(e.getMessage(), e);
        Long chatId = ChatBotUtility.getChatId(update);
        Locale locale = ChatBotUtility.getLocale(update);
        return buildMessage(chatId, locale, DEFAULT);
    }


    private SendMessage buildMessage(long chatId, Locale locale, MessageTextKey textKey) {
        String text = messageSource
                .getMessage(textKey.name(), null, locale);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        return sendMessage;
    }


    public enum MessageTextKey {
        UNREGISTERED,
        NOT_FOUND,
        ILLEGAL_ARGUMENT,

        DEFAULT
    }


    private <E extends Throwable> Map<String, BiFunction<E, Update, SendMessage>> collectExceptionHandlers() {
        Map<String, BiFunction<E, Update, SendMessage>> map = new HashMap<>();
        map.put(UnregisteredUserException.class.getSimpleName(), ((exception, update) -> this.unregisteredUserHandle((UnregisteredUserException) exception, update)));
        map.put(NotFoundException.class.getSimpleName(), ((exception, update) -> this.notFoundHandle((NotFoundException) exception, update)));
        map.put(IllegalArgumentException.class.getSimpleName(), ((exception, update) -> this.illegalArgumentHandle((IllegalArgumentException) exception, update)));
        map.put(null, (this::defaultHandle));
        return map;
    }
}

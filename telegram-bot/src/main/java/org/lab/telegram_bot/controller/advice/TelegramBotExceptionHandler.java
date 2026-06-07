package org.lab.telegram_bot.controller.advice;

import feign.FeignException;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.lab.telegram_bot.exception.IncorrectInputException;
import org.lab.telegram_bot.exception.UnregisteredUserException;
import org.lab.telegram_bot.utils.ChatBotUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
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
    public TelegramBotExceptionHandler(MessageSource messageSource,
                                       @Value("${project.variables.telegram.username}") String username) {
        log.info("Telegram-bot username: " + username);
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

    public SendMessage notFoundHandle(HttpClientErrorException.NotFound e, Update update) {
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

    public SendMessage incorrectInputHandle(IncorrectInputException e, Update update) {
        log.warn(e.getMessage(), e);
        Long chatId = ChatBotUtility.getChatId(update);
        Locale locale = ChatBotUtility.getLocale(update);
        return buildMessage(chatId, locale, INCORRECT_INPUT, e.getMessage());
    }

    public SendMessage feignClientExceptionHandle(FeignException e, Update update) {
        int status = e.status();
        Long chatId = ChatBotUtility.getChatId(update);
        Locale locale = ChatBotUtility.getLocale(update);
        return switch (status) {
            case 400 -> {
                log.warn(e.getMessage());
                yield buildMessage(chatId, locale, ILLEGAL_ARGUMENT);
            }
            case 404 -> {
                log.info(e.getMessage());
                yield buildMessage(chatId, locale, NOT_FOUND);
            }
            case 401, 403 -> {
                log.warn(e.getMessage());
                yield buildMessage(chatId, locale, UNREGISTERED);
            }
            default -> defaultHandle(e, update);
        };
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


    private SendMessage buildMessage(long chatId, Locale locale, MessageTextKey textKey, Object... args) {
        String text = messageSource
                .getMessage(textKey.name(), args, locale);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        return sendMessage;
    }


    public enum MessageTextKey {
        UNREGISTERED,
        NOT_FOUND,
        ILLEGAL_ARGUMENT,
        INCORRECT_INPUT,
        DEFAULT
    }


    private <E extends Throwable> Map<String, BiFunction<E, Update, SendMessage>> collectExceptionHandlers() {
        Map<String, BiFunction<E, Update, SendMessage>> map = new HashMap<>();
        map.put(UnregisteredUserException.class.getSimpleName(), ((exception, update) -> this.unregisteredUserHandle((UnregisteredUserException) exception, update)));
        map.put(NotFoundException.class.getSimpleName(), ((exception, update) -> this.notFoundHandle((NotFoundException) exception, update)));
        map.put(HttpClientErrorException.NotFound.class.getSimpleName(), ((exception, update) -> this.notFoundHandle((HttpClientErrorException.NotFound) exception, update)));
        map.put(IllegalArgumentException.class.getSimpleName(), ((exception, update) -> this.illegalArgumentHandle((IllegalArgumentException) exception, update)));
        map.put(IncorrectInputException.class.getSimpleName(), ((exception, update) -> this.incorrectInputHandle((IncorrectInputException) exception, update)));
        map.put(FeignException.FeignClientException.class.getSimpleName(), ((exception, update) -> this.feignClientExceptionHandle((FeignException) exception, update)));
        map.put(null, (this::defaultHandle));
        return map;
    }
}

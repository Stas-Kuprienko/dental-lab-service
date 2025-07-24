package org.lab.telegram_bot.controller;

import lombok.extern.slf4j.Slf4j;
import org.lab.telegram_bot.controller.advice.TelegramBotExceptionHandler;
import org.lab.telegram_bot.domain.command.CommandDispatcher;
import org.lab.telegram_bot.domain.element.CommandMenuList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class TelegramBotController extends TelegramLongPollingBot {

    private final CommandDispatcher commandDispatcher;
    private final TelegramBotExceptionHandler exceptionHandler;
    private final String username;


    @Autowired
    public TelegramBotController(CommandMenuList commandMenuList,
                                 CommandDispatcher commandDispatcher,
                                 TelegramBotExceptionHandler exceptionHandler,
                                 @Value("${project.variables.telegram.username}") String username,
                                 @Value("${project.variables.telegram.botToken}") String botToken) {
        super(botToken);
        this.username = username;
        this.commandDispatcher = commandDispatcher;
        this.exceptionHandler = exceptionHandler;
        this.execute(commandMenuList.getDefaultMenu());
    }


    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                commandDispatcher.apply(update.getMessage());

            } else if (update.hasCallbackQuery()) {
                commandDispatcher.apply(update.getCallbackQuery());
            }
        } catch (Exception e) {
            exceptionHandler.apply(e, update);
        }
    }

    @Override
    public <T extends Serializable, Method extends BotApiMethod<T>> T execute(Method method) {
        try {
            return super.execute(method);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
            //TODO
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T extends Serializable, Method extends BotApiMethod<T>> CompletableFuture<T> executeAsync(Method method) {
        try {
            return super.executeAsync(method);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
            //TODO
            throw new RuntimeException(e);
        }
    }
}
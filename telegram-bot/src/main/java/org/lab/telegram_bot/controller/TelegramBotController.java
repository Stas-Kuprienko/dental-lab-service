package org.lab.telegram_bot.controller;

import lombok.extern.slf4j.Slf4j;
import org.lab.telegram_bot.controller.advice.TelegramBotExceptionHandler;
import org.lab.telegram_bot.domain.command.BotCommands;
import org.lab.telegram_bot.domain.command.CommandDispatcher;
import org.lab.telegram_bot.domain.command.handlers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
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
    public TelegramBotController(CommandDispatcher commandDispatcher,
                                 TelegramBotExceptionHandler exceptionHandler,
                                 @Value("${project.variables.telegram.username}") String username,
                                 @Value("${project.variables.telegram.botToken}") String botToken) {
        super(botToken);
        this.username = username;
        this.commandDispatcher = commandDispatcher;
        this.exceptionHandler = exceptionHandler;
        setExecutorsToHandlers(commandDispatcher);
    }


    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                log.info(update.getMessage().toString());
                this.execute(commandDispatcher.apply(update.getMessage()));

            } else if (update.hasCallbackQuery()) {
                log.info(update.getCallbackQuery().toString());
                this.execute(commandDispatcher.apply(update.getCallbackQuery()));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            this.execute(exceptionHandler.apply(e, update));
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

    public void executeSendDocument(SendDocument method) {
        try {
            super.execute(method);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
            //TODO
            throw new RuntimeException(e);
        }
    }


    private void setExecutorsToHandlers(CommandDispatcher commandDispatcher) {
        StartCommandHandler startCommandHandler = (StartCommandHandler) commandDispatcher.getCommandHandler(BotCommands.START);
        startCommandHandler.setMyCommandsExecutor(this::execute);
        NewDentalWorkHandler newDentalWorkHandler = (NewDentalWorkHandler) commandDispatcher.getCommandHandler(BotCommands.NEW_DENTAL_WORK);
        newDentalWorkHandler.setExecutor(this::execute);
        DentalWorksHandler dentalWorksHandler = (DentalWorksHandler) commandDispatcher.getCommandHandler(BotCommands.DENTAL_WORKS);
        dentalWorksHandler.setExecutor(this::execute);
        ViewDentalWorkHandler viewDentalWorkHandler = (ViewDentalWorkHandler) commandDispatcher.getCommandHandler(BotCommands.VIEW_DENTAL_WORK);
        viewDentalWorkHandler.setExecutor(this::execute);
        GetReportCommandHandler getReportCommandHandler = (GetReportCommandHandler) commandDispatcher.getCommandHandler(BotCommands.GET_REPORT);
        getReportCommandHandler.setSendDocumentExecutor(this::executeSendDocument);
        CountProfitCommandHandler countProfitCommandHandler = (CountProfitCommandHandler) commandDispatcher.getCommandHandler(BotCommands.COUNT_PROFIT);
        countProfitCommandHandler.setExecutor(this::execute);
        PhotoFilesCommandHandler photoFilesCommandHandler = (PhotoFilesCommandHandler) commandDispatcher.getCommandHandler(BotCommands.PHOTO_FILES);
        photoFilesCommandHandler.setExecutor(this::execute);
    }
}
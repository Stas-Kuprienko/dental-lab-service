package org.lab.telegram_bot.domain.command.impl;

import org.lab.telegram_bot.domain.command.BotCommandHandler;
import org.lab.telegram_bot.domain.command.BotCommands;
import org.lab.telegram_bot.domain.command.CommandHandler;
import org.lab.telegram_bot.domain.session.ChatSession;
import org.lab.telegram_bot.service.DentalLabRestClientWrapper;
import org.lab.telegram_bot.service.ProductMapServiceWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Locale;

@CommandHandler(command = BotCommands.NEW_PRODUCT_TYPE)
public class NewProductTypeHandler extends BotCommandHandler {

    private final ProductMapServiceWrapper productMapService;

    @Autowired
    public NewProductTypeHandler(DentalLabRestClientWrapper dentalLabRestClient) {
        productMapService = dentalLabRestClient.PRODUCT_MAP;
    }


    @Override
    public BotApiMethod<?> handle(Message message, ChatSession session) {

        return null;
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery, ChatSession session, Locale locale) {
        return null;
    }


    enum Steps {
        CREATE,
        UPDATE,
        DELETE
    }
}

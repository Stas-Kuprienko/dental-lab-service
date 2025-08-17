package org.lab.telegram_bot.domain.command.impl;

import org.lab.telegram_bot.domain.command.BotCommandHandler;
import org.lab.telegram_bot.domain.command.BotCommands;
import org.lab.telegram_bot.domain.command.CommandHandler;
import org.lab.telegram_bot.domain.session.ChatSession;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Locale;

@CommandHandler(command = BotCommands.DENTAL_WORK_LIST)
public class DentalWorkListHandler extends BotCommandHandler {


    @Override
    public BotApiMethod<?> handle(Message message, ChatSession session) {
        return null;
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery, ChatSession session, Locale locale) {
        return null;
    }
}

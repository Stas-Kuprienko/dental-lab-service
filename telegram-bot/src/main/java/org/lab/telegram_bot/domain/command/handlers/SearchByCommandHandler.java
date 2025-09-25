package org.lab.telegram_bot.domain.command.handlers;

import org.lab.telegram_bot.domain.command.BotCommands;
import org.lab.telegram_bot.domain.command.CommandHandler;
import org.lab.telegram_bot.domain.session.ChatSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Locale;

@CommandHandler(command = BotCommands.SEARCH_BY)
public class SearchByCommandHandler extends BotCommandHandler {


    @Autowired
    public SearchByCommandHandler(MessageSource messageSource) {
        super(messageSource);
    }

    @Override
    public BotApiMethod<?> handle(Message message, ChatSession session) {
        return null;
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery, ChatSession session, Locale locale) {
        return null;
    }
}

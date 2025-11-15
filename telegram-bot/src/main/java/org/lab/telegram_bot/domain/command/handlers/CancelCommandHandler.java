package org.lab.telegram_bot.domain.command.handlers;

import org.lab.exception.BadRequestCustomException;
import org.lab.telegram_bot.domain.command.BotCommands;
import org.lab.telegram_bot.domain.command.CommandHandler;
import org.lab.telegram_bot.domain.session.ChatSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import java.util.Locale;

@CommandHandler(command = BotCommands.CANCEL)
public class CancelCommandHandler extends BotCommandHandler {


    @Autowired
    public CancelCommandHandler(MessageSource messageSource) {
        super(messageSource);
    }


    @Override
    public BotApiMethod<?> handle(Message message, ChatSession session) {
        throw new BadRequestCustomException("command is not supported");
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery, ChatSession session, Locale locale) {
        int messageId = callbackQuery.getMessage().getMessageId();
        return deleteMessage(session.getChatId(), messageId);
    }
}

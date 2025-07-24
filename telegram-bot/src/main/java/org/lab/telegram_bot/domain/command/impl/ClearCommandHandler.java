package org.lab.telegram_bot.domain.command.impl;

import org.lab.telegram_bot.domain.command.BotCommandHandler;
import org.lab.telegram_bot.domain.command.BotCommands;
import org.lab.telegram_bot.domain.command.CommandHandler;
import org.lab.telegram_bot.domain.session.ChatSession;
import org.lab.telegram_bot.domain.session.ChatSessionService;
import org.lab.telegram_bot.utils.ChatBotUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import java.util.Locale;

@CommandHandler(command = BotCommands.CLEAR)
public class ClearCommandHandler extends BotCommandHandler {

    private static final String MESSAGE_KEY = "CLEAR";

    private final MessageSource messageSource;
    private final ChatSessionService chatSessionService;


    @Autowired
    public ClearCommandHandler(MessageSource messageSource, ChatSessionService chatSessionService) {
        this.messageSource = messageSource;
        this.chatSessionService = chatSessionService;
    }


    @Override
    public BotApiMethod<?> handle(Message message, ChatSession session) {
        Locale locale = ChatBotUtility.getLocale(message);
        String text = messageSource.getMessage(MESSAGE_KEY, null, locale);
        session.clear();
        chatSessionService.save(session);
        return createSendMessage(session.getChatId(), text);
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery, ChatSession session, Locale locale) {
        String textToSend = messageSource.getMessage(MESSAGE_KEY, null, locale);
        session.clear();
        chatSessionService.save(session);
        return createSendMessage(session.getChatId(), textToSend);
    }
}

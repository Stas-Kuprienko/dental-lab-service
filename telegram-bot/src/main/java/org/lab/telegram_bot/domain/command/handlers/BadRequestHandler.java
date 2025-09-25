package org.lab.telegram_bot.domain.command.handlers;

import org.lab.telegram_bot.controller.advice.TelegramBotExceptionHandler;
import org.lab.telegram_bot.domain.session.ChatSession;
import org.lab.telegram_bot.utils.ChatBotUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import java.util.Locale;

@Component("badRequestHandler")
public class BadRequestHandler extends BotCommandHandler {


    @Autowired
    public BadRequestHandler(MessageSource messageSource) {
        super(messageSource);
    }


    @Override
    public BotApiMethod<?> handle(Message message, ChatSession session) {
        Long chatId = message.getChatId();
        Locale locale = ChatBotUtility.getLocale(message);

        String text = messageSource
                .getMessage(TelegramBotExceptionHandler.MessageTextKey.ILLEGAL_ARGUMENT.name(), null, locale);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        return sendMessage;
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery, ChatSession session, Locale locale) {
        Long chatId = session.getChatId();

        String textToSend = messageSource
                .getMessage("ILLEGAL_ARGUMENT", null, locale);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(textToSend);
        return sendMessage;
    }
}

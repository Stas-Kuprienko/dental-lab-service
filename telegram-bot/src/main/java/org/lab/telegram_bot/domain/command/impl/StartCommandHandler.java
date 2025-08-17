package org.lab.telegram_bot.domain.command.impl;

import org.lab.telegram_bot.controller.TelegramBotController;
import org.lab.telegram_bot.domain.command.BotCommandHandler;
import org.lab.telegram_bot.domain.command.BotCommands;
import org.lab.telegram_bot.domain.command.CommandHandler;
import org.lab.telegram_bot.domain.element.CommandMenuList;
import org.lab.telegram_bot.domain.session.ChatSession;
import org.lab.telegram_bot.utils.ChatBotUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import java.util.Locale;
import java.util.function.Consumer;

@CommandHandler(command = BotCommands.START)
public class StartCommandHandler extends BotCommandHandler {

    private final MessageSource messageSource;
    private final CommandMenuList commandMenuList;
    private final Consumer<SetMyCommands> executor;


    @Autowired
    public StartCommandHandler(MessageSource messageSource,
                               CommandMenuList commandMenuList,
                               TelegramBotController telegramBotController) {
        this.messageSource = messageSource;
        this.commandMenuList = commandMenuList;
        this.executor = telegramBotController::execute;
    }


    @Override
    public BotApiMethod<?> handle(Message message, ChatSession session) {
        String userName = ChatBotUtility.getUsername(message);
        Locale locale = ChatBotUtility.getLocale(message);
        String text = messageSource.getMessage(BotCommands.START.name(), new Object[]{userName}, locale);
        SetMyCommands commands = commandMenuList.getMenuForLocale(locale);
        executor.accept(commands);
        return createSendMessage(session.getChatId(), text);
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery, ChatSession session, Locale locale) {
        String userName = ChatBotUtility.getUsername(callbackQuery);
        String text = messageSource.getMessage(BotCommands.START.name(), new Object[]{userName}, locale);
        SetMyCommands commands = commandMenuList.getMenuForLocale(locale);
        executor.accept(commands);
        return createSendMessage(session.getChatId(), text);
    }
}

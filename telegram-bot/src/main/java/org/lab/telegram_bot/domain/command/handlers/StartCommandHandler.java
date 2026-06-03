package org.lab.telegram_bot.domain.command.handlers;

import org.lab.telegram_bot.domain.command.BotCommands;
import org.lab.telegram_bot.domain.command.CommandHandler;
import org.lab.telegram_bot.domain.element.CommandMenuList;
import org.lab.telegram_bot.domain.session.ChatSession;
import org.lab.telegram_bot.exception.ApplicationCustomException;
import org.lab.telegram_bot.utils.ChatBotUtility;
import org.lab.telegram_bot.utils.metrics.TGBotMetrics;
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

    private final CommandMenuList commandMenuList;
    private final TGBotMetrics metrics;
    private Consumer<SetMyCommands> executor;


    @Autowired
    public StartCommandHandler(MessageSource messageSource,
                               TGBotMetrics metrics,
                               CommandMenuList commandMenuList) {
        super(messageSource);
        this.commandMenuList = commandMenuList;
        this.metrics = metrics;
    }


    @Override
    public BotApiMethod<?> handle(Message message, ChatSession session) {
        if (executor == null) {
            throw new ApplicationCustomException("The set commands executor 'Consumer<SetMyCommands>' is null");
        }
        String userName = ChatBotUtility.getUsername(message);
        Locale locale = ChatBotUtility.getLocale(message);
        String text = messageSource.getMessage(BotCommands.START.name(), new Object[]{userName}, locale);
        SetMyCommands commands = commandMenuList.getMenuForLocale(locale);
        executor.accept(commands);
        metrics.getBotStartCommands().increment();
        return createSendMessage(session.getChatId(), text);
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery, ChatSession session, Locale locale) {
        if (executor == null) {
            throw new ApplicationCustomException("The set commands executor 'Consumer<SetMyCommands>' is null");
        }
        String userName = ChatBotUtility.getUsername(callbackQuery);
        String text = messageSource.getMessage(BotCommands.START.name(), new Object[]{userName}, locale);
        SetMyCommands commands = commandMenuList.getMenuForLocale(locale);
        executor.accept(commands);
        metrics.getBotStartCommands().increment();
        return createSendMessage(session.getChatId(), text);
    }

    public void setMyCommandsExecutor(Consumer<SetMyCommands> executor) {
        this.executor = executor;
    }
}

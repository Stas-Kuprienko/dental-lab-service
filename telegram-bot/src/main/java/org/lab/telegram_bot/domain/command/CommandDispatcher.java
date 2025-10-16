package org.lab.telegram_bot.domain.command;

import lombok.extern.slf4j.Slf4j;
import org.lab.telegram_bot.domain.command.handlers.BotCommandHandler;
import org.lab.telegram_bot.domain.session.ChatSession;
import org.lab.telegram_bot.domain.session.ChatSessionService;
import org.lab.telegram_bot.exception.UnregisteredUserException;
import org.lab.telegram_bot.utils.ChatBotUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Component
public class CommandDispatcher {

    private final Map<String, BotCommandHandler> commandHandlerStore;
    private final ChatSessionService chatSessionService;


    @Autowired
    public CommandDispatcher(ApplicationContext applicationContext,
                             ChatSessionService chatSessionService) {
        commandHandlerStore = collectHandlers(applicationContext);
        this.chatSessionService = chatSessionService;
    }


    public BotApiMethod<?> apply(Message message) {
        BotCommandHandler handler;
        ChatSession session;
        try {
            session = chatSessionService.get(message.getChatId());
            if (message.hasPhoto()) {
                handler = commandHandlerStore.get(BotCommands.PHOTO_FILES.value);
            } else {
                handler = commandHandlerStore.get(message.getText());
                if (handler != null) {
                    session.setStep(0);
                } else {
                    BotCommands sessionContextCommand = session.getCommand();
                    if (sessionContextCommand != null) {
                        handler = commandHandlerStore.get(sessionContextCommand.value);
                    } else {
                        handler = commandHandlerStore.get(null);
                    }
                }
            }
        } catch (UnregisteredUserException e) {
            log.info(e.getMessage());
            handler = commandHandlerStore.get(BotCommands.START.value);
            session = chatSessionService.create(message.getChatId(), null);
        }
        log.info("Command handler {} is applied", handler.getClass().getSimpleName());
        return handler.handle(message, session);
    }

    public  BotApiMethod<?> apply(CallbackQuery callbackQuery) {
        // Callback query pattern - ${COMMAND}:${STEP}:${DATA} (for example, PRODUCT_MAP:1:CERAMIC)
        ChatSession session = chatSessionService.get(callbackQuery.getFrom().getId());
        Locale locale = ChatBotUtility.getLocale(callbackQuery);
        String[] callback = ChatBotUtility.callBackQueryParse(callbackQuery);
        BotCommands command = BotCommands.valueOf(callback[0]);
        int step = Integer.parseInt(callback[1]);
        session.setCommand(command);
        session.setStep(step);
        BotCommandHandler handler = commandHandlerStore.get(command.value);
        if (handler == null) {
            handler = commandHandlerStore.get(null);
        }
        log.info("Command handler {} is applied", handler.getClass().getSimpleName());
        return handler.handle(callbackQuery, session, locale);
    }

    public BotCommandHandler getCommandHandler(BotCommands botCommand) {
        return commandHandlerStore.get(botCommand.value);
    }


    private Map<String, BotCommandHandler> collectHandlers(ApplicationContext applicationContext) {
        Map<String, BotCommandHandler> commandHandlers = new HashMap<>();
        applicationContext
                .getBeansOfType(BotCommandHandler.class)
                .forEach((key, value) -> {
                    CommandHandler handlerAnnotation = value.getClass().getAnnotation(CommandHandler.class);
                    if (handlerAnnotation != null) {
                        String command = handlerAnnotation.command().value;
                        commandHandlers.put(command, value);
                        log.info("Handler for bot command '{}' is registered", command);
                    }
                });
        BotCommandHandler badRequestHandler = applicationContext.getBean("badRequestHandler", BotCommandHandler.class);
        commandHandlers.put(null, badRequestHandler);
        return commandHandlers;
    }
}

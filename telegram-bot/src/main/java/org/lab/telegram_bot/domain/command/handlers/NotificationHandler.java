package org.lab.telegram_bot.domain.command.handlers;

import org.lab.dental.feignclient.UserService;
import org.lab.enums.MailingType;
import org.lab.model.User;
import org.lab.telegram_bot.domain.command.BotCommands;
import org.lab.telegram_bot.domain.command.CommandHandler;
import org.lab.telegram_bot.domain.command.TextKeys;
import org.lab.telegram_bot.domain.element.ButtonKeys;
import org.lab.telegram_bot.domain.element.KeyboardBuilderKit;
import org.lab.telegram_bot.domain.session.ChatSession;
import org.lab.telegram_bot.domain.session.ChatSessionService;
import org.lab.telegram_bot.utils.ChatBotUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import java.util.List;
import java.util.Locale;

@CommandHandler(command = BotCommands.NOTIFICATIONS)
public class NotificationHandler extends BotCommandHandler {

    private final UserService userService;
    private final KeyboardBuilderKit keyboardBuilderKit;
    private final ChatSessionService chatSessionService;


    @Autowired
    public NotificationHandler(MessageSource messageSource, UserService userService,
                               KeyboardBuilderKit keyboardBuilderKit,
                               ChatSessionService chatSessionService) {
        super(messageSource);
        this.userService = userService;
        this.keyboardBuilderKit = keyboardBuilderKit;
        this.chatSessionService = chatSessionService;
    }


    @Override
    public BotApiMethod<?> handle(Message message, ChatSession session) {
        Locale locale = ChatBotUtility.getLocale(message);
        int messageId = message.getMessageId();
        Steps step = getStep(session);
        return switch (step) {
            case STATUS -> status(session, locale);
            case ENABLE -> enableNotifications(session, locale, messageId);
            case DISABLE -> disableNotifications(session, locale, messageId);
        };
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery, ChatSession session, Locale locale) {
        int messageId = callbackQuery.getMessage().getMessageId();
        Steps step = getStep(session);
        return switch (step) {
            case STATUS -> status(session, locale);
            case ENABLE -> enableNotifications(session, locale, messageId);
            case DISABLE -> disableNotifications(session, locale, messageId);
        };
    }


    private SendMessage status(ChatSession session, Locale locale) {
        User user = userService.getById(session.getUserId());
        MailingType mailingType = user.getMailingType();
        String notificationStatus = mailingType != null ? mailingType.name()
                : messageSource.getMessage(ButtonKeys.NO.name(), null, locale);
        String text = messageSource.getMessage(TextKeys.NOTIFICATION_STATUS.name(), new Object[]{notificationStatus}, locale);
        Steps step;
        ButtonKeys buttonKey;
        if (mailingType == null || mailingType.equals(MailingType.EMAIL)) {
            buttonKey = ButtonKeys.ENABLE;
            step = Steps.ENABLE;
        } else {
            buttonKey = ButtonKeys.DISABLE;
            step = Steps.DISABLE;
        }
        String callBackQueryPrefix = ChatBotUtility.callBackQueryPrefix(BotCommands.NOTIFICATIONS, step.ordinal());
        InlineKeyboardButton inlineKeyboardButton = keyboardBuilderKit.callbackButton(buttonKey, callBackQueryPrefix, locale);
        session.setCommand(BotCommands.NOTIFICATIONS);
        session.setStep(step.ordinal());
        chatSessionService.save(session);
        return createSendMessage(session.getChatId(), text, keyboardBuilderKit.inlineKeyboard(List.of(inlineKeyboardButton)));
    }

    private EditMessageText enableNotifications(ChatSession session, Locale locale, int messageId) {
        userService.subscribeForNotifications(MailingType.TELEGRAM, session.getUserId());
        String text = messageSource.getMessage(TextKeys.NOTIFICATION_STATUS.name(), new Object[]{MailingType.TELEGRAM.name()}, locale);
        Steps step = Steps.DISABLE;
        String callBackQueryPrefix = ChatBotUtility.callBackQueryPrefix(BotCommands.NOTIFICATIONS, step.ordinal());
        InlineKeyboardButton inlineKeyboardButton = keyboardBuilderKit.callbackButton(ButtonKeys.DISABLE, callBackQueryPrefix, locale);
        session.setCommand(BotCommands.NOTIFICATIONS);
        session.setStep(step.ordinal());
        chatSessionService.save(session);
        return editMessageText(session.getChatId(), messageId, text, keyboardBuilderKit.inlineKeyboard(List.of(inlineKeyboardButton)));

    }

    private EditMessageText disableNotifications(ChatSession session, Locale locale, int messageId) {
        userService.unsubscribeForNotifications(session.getUserId());
        String textNo = messageSource.getMessage(ButtonKeys.NO.name(), null, locale);
        String text = messageSource.getMessage(TextKeys.NOTIFICATION_STATUS.name(), new Object[]{textNo}, locale);
        Steps step = Steps.ENABLE;
        String callBackQueryPrefix = ChatBotUtility.callBackQueryPrefix(BotCommands.NOTIFICATIONS, step.ordinal());
        InlineKeyboardButton inlineKeyboardButton = keyboardBuilderKit.callbackButton(ButtonKeys.ENABLE, callBackQueryPrefix, locale);
        session.setCommand(BotCommands.NOTIFICATIONS);
        session.setStep(step.ordinal());
        chatSessionService.save(session);
        return editMessageText(session.getChatId(), messageId, text, keyboardBuilderKit.inlineKeyboard(List.of(inlineKeyboardButton)));

    }

    private Steps getStep(ChatSession session) {
        return Steps.values()[session.getStep()];
    }


    enum Steps {
        STATUS,
        ENABLE,
        DISABLE
    }
}

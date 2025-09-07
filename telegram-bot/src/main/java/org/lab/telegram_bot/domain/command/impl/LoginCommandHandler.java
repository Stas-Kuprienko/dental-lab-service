package org.lab.telegram_bot.domain.command.impl;

import org.lab.request.NewTelegramOtpLink;
import org.lab.request.OtpRequest;
import org.lab.telegram_bot.controller.advice.TelegramBotExceptionHandler;
import org.lab.telegram_bot.domain.command.BotCommandHandler;
import org.lab.telegram_bot.domain.command.BotCommands;
import org.lab.telegram_bot.domain.command.CommandHandler;
import org.lab.telegram_bot.domain.element.CommandMenuList;
import org.lab.telegram_bot.domain.session.ChatSession;
import org.lab.telegram_bot.domain.session.ChatSessionService;
import org.lab.telegram_bot.exception.ConfigurationCustomException;
import org.lab.telegram_bot.service.DentalLabRestClientWrapper;
import org.lab.telegram_bot.service.TelegramChatServiceWrapper;
import org.lab.telegram_bot.utils.ChatBotUtility;
import org.lab.telegram_bot.utils.LinkingKeyGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@CommandHandler(command = BotCommands.LOGIN)
public class LoginCommandHandler extends BotCommandHandler {

    private static final String LINK_CREATED = "LINK_CREATED";
    private static final String LINK_CREATED_FOR_LINKED_USER = "LINK_CREATED_FOR_LINKED_USER";
    private static final String LINK_EXPIRED = "LINK_EXPIRED";
    private static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
    private static final String LOGIN_FAIL = "LOGIN_FAIL";
    private static final int KEY_DURATION_IN_MINUTES = 10;

    private final LinkingKeyGenerator keyGenerator;
    private final TelegramChatServiceWrapper telegramChatService;
    private final CommandMenuList commandMenuList;
    private final MessageSource messageSource;
    private final ChatSessionService chatSessionService;
    private final String bindingPage;
    private final ConcurrentHashMap<Long, UserLink> userLinkMap;
    private Consumer<SetMyCommands> executor;


    @Autowired
    public LoginCommandHandler(LinkingKeyGenerator keyGenerator,
                               DentalLabRestClientWrapper dentalLabRestClient,
                               CommandMenuList commandMenuList,
                               MessageSource messageSource,
                               ChatSessionService chatSessionService,
                               @Value("${project.variables.dental-lab-site.url}") String serviceSiteUrl) {
        this.keyGenerator = keyGenerator;
        telegramChatService = dentalLabRestClient.TELEGRAM_CHATS;
        this.commandMenuList = commandMenuList;
        this.messageSource = messageSource;
        this.chatSessionService = chatSessionService;
        this.bindingPage = serviceSiteUrl + "/telegram-bind/link/";
        userLinkMap = new ConcurrentHashMap<>();
    }


    @Override
    public BotApiMethod<?> handle(Message message, ChatSession session) {
        Locale locale = ChatBotUtility.getLocale(message);
        String userName = ChatBotUtility.getUsername(message);
        String messageText = message.getText();
        Steps step = getStep(session);
        session.setCommand(BotCommands.LOGIN);
        return switch (step) {
            case CREATE_LINK -> createLink(session, locale, userName);
            case INPUT_OTP -> inputOtp(session, locale, messageText, userName);
        };
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery, ChatSession session, Locale locale) {
        String text = messageSource.getMessage(TelegramBotExceptionHandler.MessageTextKey.ILLEGAL_ARGUMENT.name(), null, locale);
        return createSendMessage(session.getChatId(), text);
    }

    public void setMyCommandsExecutor(Consumer<SetMyCommands> executor) {
        this.executor = executor;
    }


    private SendMessage createLink(ChatSession session, Locale locale, String userName) {
        long chatId = session.getChatId();
        String key = keyGenerator.generate(31);
        NewTelegramOtpLink newTelegramOtpLink = NewTelegramOtpLink.builder()
                .key(key)
                .chatId(chatId)
                .build();
        telegramChatService.createLink(newTelegramOtpLink);
        userLinkMap.put(chatId, new UserLink(key));
        String link = bindingPage + key;
        String messageKey = session.getUserId() == null ? LINK_CREATED : LINK_CREATED_FOR_LINKED_USER;
        String text = messageSource.getMessage(messageKey, new Object[]{userName, link}, locale);
        session.setStep(Steps.INPUT_OTP.ordinal());
        chatSessionService.save(session);
        return createSendMessage(chatId, text);
    }

    private SendMessage inputOtp(ChatSession session, Locale locale, String messageText, String userName) {
        if (executor == null) {
            throw new ConfigurationCustomException("The set commands executor 'Consumer<SetMyCommands>' is null");
        }
        long chatId = session.getChatId();
        UserLink userLink = userLinkMap.get(chatId);
        String text;
        if (userLink == null || userLink.isExpired()) {
            text = messageSource.getMessage(LINK_EXPIRED, null, locale);
        } else {
            UUID userId = telegramChatService.bindTelegram(userLink.key, new OtpRequest(messageText));
            chatSessionService.create(chatId, userId);
            text = messageSource.getMessage(LOGIN_SUCCESS, new Object[]{userName}, locale);
        }
        userLinkMap.remove(chatId);
        SetMyCommands commands = commandMenuList.getMenuForLocale(locale);
        executor.accept(commands);
        return createSendMessage(chatId, text);
    }

    private Steps getStep(ChatSession session) {
        return Steps.values()[session.getStep()];
    }


    enum Steps {
        CREATE_LINK,
        INPUT_OTP
    }


    static final class UserLink {

        private final String key;
        private final LocalDateTime created;

        private UserLink(String key) {
            this.key = key;
            this.created = LocalDateTime.now();
        }

        boolean isExpired() {
            return created.isAfter(LocalDateTime.now().plusMinutes(KEY_DURATION_IN_MINUTES));
        }
    }
}

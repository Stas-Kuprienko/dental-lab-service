package org.lab.telegram_bot.domain.command.handlers;

import org.lab.model.DentalWork;
import org.lab.telegram_bot.domain.command.BotCommands;
import org.lab.telegram_bot.domain.command.CommandHandler;
import org.lab.telegram_bot.domain.command.TextKeys;
import org.lab.telegram_bot.domain.element.ButtonKeys;
import org.lab.telegram_bot.domain.element.KeyboardBuilderKit;
import org.lab.telegram_bot.domain.session.ChatSession;
import org.lab.telegram_bot.domain.session.ChatSessionService;
import org.lab.telegram_bot.service.DentalLabRestClientWrapper;
import org.lab.telegram_bot.service.DentalWorkServiceWrapper;
import org.lab.telegram_bot.utils.ChatBotUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.Locale;

@CommandHandler(command = BotCommands.SEARCH_BY)
public class SearchByCommandHandler extends BotCommandHandler {

    private final DentalWorkServiceWrapper dentalWorkService;
    private final ChatSessionService chatSessionService;
    private final KeyboardBuilderKit keyboardBuilderKit;


    @Autowired
    public SearchByCommandHandler(MessageSource messageSource,
                                  DentalLabRestClientWrapper dentalLabRestClient,
                                  ChatSessionService chatSessionService,
                                  KeyboardBuilderKit keyboardBuilderKit) {
        super(messageSource);
        this.dentalWorkService = dentalLabRestClient.DENTAL_WORKS;
        this.chatSessionService = chatSessionService;
        this.keyboardBuilderKit = keyboardBuilderKit;
    }


    @Override
    public BotApiMethod<?> handle(Message message, ChatSession session) {
        Locale locale = ChatBotUtility.getLocale(message);
        String messageText = message.getText();
        int messageId = message.getMessageId();
        Steps step = getStep(session);
        return switch (step) {
            case INPUT -> input(session, locale);
            case SEARCH_WORK -> search(session, locale, messageText, messageId);
        };
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery, ChatSession session, Locale locale) {
        String messageText = callbackQuery.getData();
        int messageId = callbackQuery.getMessage().getMessageId();
        Steps step = getStep(session);
        return switch (step) {
            case INPUT -> input(session, locale);
            case SEARCH_WORK -> search(session, locale, messageText, messageId);
        };
    }


    private SendMessage input(ChatSession session, Locale locale) {
        String text = messageSource.getMessage(TextKeys.INPUT_VALUE_FOR_SEARCH.name(), null, locale);
        session.setCommand(BotCommands.SEARCH_BY);
        session.setStep(Steps.SEARCH_WORK.ordinal());
        chatSessionService.save(session);
        return createSendMessage(session.getChatId(), text);
    }

    private SendMessage search(ChatSession session, Locale locale, String messageText, int messageId) {
        String[] values = messageText.split("\n");
        List<DentalWork> dentalWorks;
        if (values.length == 2) {
            dentalWorks = dentalWorkService.searchDentalWorks(values[1], values[0], session.getUserId());
        } else {
            dentalWorks = dentalWorkService.searchDentalWorks(null, values[0], session.getUserId());
        }
        String text = workListToMessage(dentalWorks, locale);
        InlineKeyboardButton selectButton = buildSelectItemButton(locale, messageId);
        InlineKeyboardMarkup keyboardMarkup = keyboardBuilderKit.inlineKeyboard(List.of(selectButton));
        session.reset();
        chatSessionService.save(session);
        return createSendMessage(session.getChatId(), text, keyboardMarkup);
    }

    private InlineKeyboardButton buildSelectItemButton(Locale locale, int messageId) {
        String callbackQueryPrefix = ChatBotUtility.callBackQueryPrefix(BotCommands.DENTAL_WORKS, DentalWorksHandler.Steps.SELECT_ITEM.ordinal());
        String callbackQueryData = callbackQueryPrefix + messageId;
        String callbackLabel = messageSource.getMessage(ButtonKeys.SELECT_ITEM.name(), null, locale);
        return keyboardBuilderKit.callbackButton(callbackLabel, callbackQueryData);
    }

    private Steps getStep(ChatSession session) {
        return Steps.values()[session.getStep()];
    }


    enum Steps {
        INPUT,
        SEARCH_WORK
    }
}

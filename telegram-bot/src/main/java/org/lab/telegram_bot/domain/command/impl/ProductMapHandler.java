package org.lab.telegram_bot.domain.command.impl;

import org.lab.model.ProductMap;
import org.lab.model.ProductType;
import org.lab.telegram_bot.domain.command.BotCommandHandler;
import org.lab.telegram_bot.domain.command.BotCommands;
import org.lab.telegram_bot.domain.command.CommandHandler;
import org.lab.telegram_bot.domain.element.ButtonKeys;
import org.lab.telegram_bot.domain.element.KeyboardBuilderKit;
import org.lab.telegram_bot.domain.session.ChatSession;
import org.lab.telegram_bot.domain.session.ChatSessionService;
import org.lab.telegram_bot.service.DentalLabRestClientWrapper;
import org.lab.telegram_bot.service.ProductMapServiceWrapper;
import org.lab.telegram_bot.utils.ChatBotUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import java.util.List;
import java.util.Locale;

@CommandHandler(command = BotCommands.PRODUCT_MAP)
public class ProductMapHandler extends BotCommandHandler {

    private final ProductMapServiceWrapper productMapService;
    private final KeyboardBuilderKit keyboardBuilderKit;
    private final MessageSource messageSource;
    private final ChatSessionService chatSessionService;


    @Autowired
    public ProductMapHandler(DentalLabRestClientWrapper dentalLabRestClient,
                             KeyboardBuilderKit keyboardBuilderKit,
                             MessageSource messageSource,
                             ChatSessionService chatSessionService) {
        this.productMapService = dentalLabRestClient.PRODUCT_MAP;
        this.keyboardBuilderKit = keyboardBuilderKit;
        this.messageSource = messageSource;
        this.chatSessionService = chatSessionService;
    }


    @Override
    public BotApiMethod<?> handle(Message message, ChatSession session) {
        Locale locale = ChatBotUtility.getLocale(message);
        String messageText = message.getText();
        Steps step = getStep(session);
        return switch (step) {
            case GET -> get(session, locale);
            case UPDATE -> update(session, locale, messageText);
            case DELETE -> delete(session, locale, messageText);
        };
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery, ChatSession session, Locale locale) {
        String messageText = callbackQuery.getData();
        Steps step = getStep(session);
        return switch (step) {
            case GET -> get(session, locale);
            case UPDATE -> update(session, locale, messageText);
            case DELETE -> delete(session, locale, messageText);
        };
    }


    private SendMessage get(ChatSession session, Locale locale) {
        ProductMap productMap = productMapService.findAll(session.getUserId());
        String text = buildProductMapMessageText(productMap);
        String callbackQueryPrefix = ChatBotUtility.callBackQueryPrefix(BotCommands.PRODUCT_MAP, 0);
        InlineKeyboardButton update = keyboardBuilderKit.callbackButton(ButtonKeys.UPDATE, callbackQueryPrefix,locale);
        InlineKeyboardButton delete = keyboardBuilderKit.callbackButton(ButtonKeys.DELETE, callbackQueryPrefix,locale);
        var keyboard = keyboardBuilderKit.inlineKeyboard(List.of(update, delete));
        session.setStep(0);
        session.setCommand(BotCommands.PRODUCT_MAP);
        chatSessionService.save(session);
        return createSendMessage(session.getChatId(), text, keyboard);
    }

    private SendMessage update(ChatSession session, Locale locale, String messageText) {
        return null;
    }

    private SendMessage delete(ChatSession session, Locale locale, String messageText) {
        return null;
    }

    private String buildProductMapMessageText(ProductMap productMap) {
        StringBuilder stringBuilder = new StringBuilder();
        String splitter = "---------";
        for (ProductType pt : productMap.getEntries()) {
            stringBuilder.append(splitter)
                    .append('\n')
                    .append(pt.getTitle())
                    .append(" : ")
                    .append(pt.getPrice())
                    .append('\n');
        }
        stringBuilder.append(splitter);
        return stringBuilder.toString();
    }

    private Steps getStep(ChatSession session) {
        return Steps.values()[session.getStep()];
    }


    enum Steps {
        GET,
        UPDATE,
        DELETE
    }
}

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
import org.lab.telegram_bot.service.ProductMapMvcService;
import org.lab.telegram_bot.utils.ChatBotUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@CommandHandler(command = BotCommands.PRODUCT_MAP)
public class ProductMapHandler extends BotCommandHandler {

    private final ProductMapMvcService productMapService;
    private final KeyboardBuilderKit keyboardBuilderKit;
    private final MessageSource messageSource;
    private final ChatSessionService chatSessionService;


    @Autowired
    public ProductMapHandler(ProductMapMvcService productMapService,
                             KeyboardBuilderKit keyboardBuilderKit,
                             MessageSource messageSource,
                             ChatSessionService chatSessionService) {
        this.productMapService = productMapService;
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
            case GET_PRODUCT_TYPES -> get(session, locale);
            case PRODUCT_TYPE_LIST_FOR_UPDATE -> updateList(session, locale, message.getMessageId());
            case SELECT_PRODUCT_TYPE_TO_UPDATE -> selectToUpdate(session, locale, messageText, message.getMessageId());
            case UPDATE_PRODUCT_TYPE_INPUT -> updateInput(session, locale, messageText);
            case PRODUCT_TYPE_LIST_FOR_DELETE -> deleteList(session, locale, message.getMessageId());
            case SELECT_PRODUCT_TYPE_TO_DELETE -> selectToDelete(session, locale, messageText, message.getMessageId());
        };
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery, ChatSession session, Locale locale) {
        String messageText = callbackQuery.getData();
        Steps step = getStep(session);
        return switch (step) {
            case GET_PRODUCT_TYPES -> get(session, locale);
            case PRODUCT_TYPE_LIST_FOR_UPDATE -> updateList(session, locale, callbackQuery.getMessage().getMessageId());
            case SELECT_PRODUCT_TYPE_TO_UPDATE ->
                    selectToUpdate(session, locale, messageText, callbackQuery.getMessage().getMessageId());
            case UPDATE_PRODUCT_TYPE_INPUT -> updateInput(session, locale, messageText);
            case PRODUCT_TYPE_LIST_FOR_DELETE -> deleteList(session, locale, callbackQuery.getMessage().getMessageId());
            case SELECT_PRODUCT_TYPE_TO_DELETE ->
                    selectToDelete(session, locale, messageText, callbackQuery.getMessage().getMessageId());
        };
    }


    private SendMessage get(ChatSession session, Locale locale) {
        ProductMap productMap = productMapService.findAll(session.getUserId());
        return sendProductMapAsMessage(productMap, session, locale);
    }

    private EditMessageText get(ChatSession session, Locale locale, int messageId) {
        ProductMap productMap = productMapService.findAll(session.getUserId());
        return sendProductMapAsMessage(productMap, session, locale, messageId);
    }

    private EditMessageText updateList(ChatSession session, Locale locale, int messageId) {
        String text = messageSource.getMessage(Steps.PRODUCT_TYPE_LIST_FOR_UPDATE.name(), null, locale);
        InlineKeyboardMarkup keyboardMarkup = productMapAsCallbackQuery(session, locale, Steps.SELECT_PRODUCT_TYPE_TO_UPDATE);
        session.setStep(Steps.SELECT_PRODUCT_TYPE_TO_UPDATE.ordinal());
        session.setCommand(BotCommands.PRODUCT_MAP);
        chatSessionService.save(session);
        return editMessageText(session.getChatId(), messageId, text, keyboardMarkup);
    }

    private EditMessageText selectToUpdate(ChatSession session, Locale locale, String messageText, int messageId) {
        String[] callbackData = ChatBotUtility.callBackQueryParse(messageText);
        if (callbackData[2].equals(ButtonKeys.CANCEL.name())) {
            String text = messageSource.getMessage(CANCEL_RESPONSE, null, locale);
            session.clear();
            chatSessionService.save(session);
            return editMessageText(session.getChatId(), messageId, text);
        }
        UUID productTypeId = UUID.fromString(callbackData[2]);
        ProductType productType = productMapService.findById(productTypeId, session.getUserId());
        session.addAttribute(Attributes.PRODUCT_TYPE_ID, productTypeId);
        String text = messageSource.getMessage(Steps.SELECT_PRODUCT_TYPE_TO_UPDATE.name(), new Object[]{productType.getTitle()}, locale);
        session.setStep(Steps.UPDATE_PRODUCT_TYPE_INPUT.ordinal());
        session.setCommand(BotCommands.PRODUCT_MAP);
        chatSessionService.save(session);
        return editMessageText(session.getChatId(), messageId, text);
    }

    private SendMessage updateInput(ChatSession session, Locale locale, String messageText) {
        float newPrice = Float.parseFloat(messageText);
        UUID productTypeId = (UUID) session.getAttribute(Attributes.PRODUCT_TYPE_ID);
        if (productTypeId == null) {
            throw new IllegalArgumentException("ChatSession attribute '%s' is null".formatted(Attributes.PRODUCT_TYPE_ID));
        }
        ProductMap productMap = productMapService.updatePrice(productTypeId, newPrice, session.getUserId());
        session.removeAttribute(Attributes.PRODUCT_TYPE_ID);
        return sendProductMapAsMessage(productMap, session, locale);
    }

    private EditMessageText deleteList(ChatSession session, Locale locale, int messageId) {
        String text = messageSource.getMessage(Steps.PRODUCT_TYPE_LIST_FOR_DELETE.name(), null, locale);
        InlineKeyboardMarkup keyboardMarkup = productMapAsCallbackQuery(session, locale, Steps.SELECT_PRODUCT_TYPE_TO_DELETE);
        session.setStep(Steps.SELECT_PRODUCT_TYPE_TO_DELETE.ordinal());
        session.setCommand(BotCommands.PRODUCT_MAP);
        chatSessionService.save(session);
        return editMessageText(session.getChatId(), messageId, text, keyboardMarkup);
    }

    private EditMessageText selectToDelete(ChatSession session, Locale locale, String messageText, int messageId) {
        String[] callbackData = ChatBotUtility.callBackQueryParse(messageText);
        if (callbackData[2].equals(ButtonKeys.BACK.name())) {
            return get(session, locale, messageId);
        }
        UUID productTypeId = UUID.fromString(callbackData[2]);
        ProductMap productMap = productMapService.delete(productTypeId, session.getUserId());
        return sendProductMapAsMessage(productMap, session, locale, messageId);
    }

    private SendMessage sendProductMapAsMessage(ProductMap productMap, ChatSession session, Locale locale) {
        String text = productMapAsMessageText(productMap);
        String callbackQueryPrefix = ChatBotUtility.callBackQueryPrefix(BotCommands.PRODUCT_MAP, Steps.PRODUCT_TYPE_LIST_FOR_UPDATE.ordinal());
        InlineKeyboardButton update = keyboardBuilderKit.callbackButton(ButtonKeys.UPDATE, callbackQueryPrefix, locale);
        callbackQueryPrefix = ChatBotUtility.callBackQueryPrefix(BotCommands.PRODUCT_MAP, Steps.PRODUCT_TYPE_LIST_FOR_DELETE.ordinal());
        InlineKeyboardButton delete = keyboardBuilderKit.callbackButton(ButtonKeys.DELETE, callbackQueryPrefix, locale);
        var keyboard = keyboardBuilderKit.inlineKeyboard(List.of(update, delete));
        session.setStep(0);
        session.setCommand(BotCommands.PRODUCT_MAP);
        chatSessionService.save(session);
        return createSendMessage(session.getChatId(), text, keyboard);
    }

    private EditMessageText sendProductMapAsMessage(ProductMap productMap, ChatSession session, Locale locale, int messageId) {
        String text = productMapAsMessageText(productMap);
        String callbackQueryPrefix = ChatBotUtility.callBackQueryPrefix(BotCommands.PRODUCT_MAP, Steps.PRODUCT_TYPE_LIST_FOR_UPDATE.ordinal());
        InlineKeyboardButton update = keyboardBuilderKit.callbackButton(ButtonKeys.UPDATE, callbackQueryPrefix, locale);
        callbackQueryPrefix = ChatBotUtility.callBackQueryPrefix(BotCommands.PRODUCT_MAP, Steps.PRODUCT_TYPE_LIST_FOR_DELETE.ordinal());
        InlineKeyboardButton delete = keyboardBuilderKit.callbackButton(ButtonKeys.DELETE, callbackQueryPrefix, locale);
        var keyboard = keyboardBuilderKit.inlineKeyboard(List.of(update, delete));
        session.setStep(0);
        session.setCommand(BotCommands.PRODUCT_MAP);
        chatSessionService.save(session);
        return editMessageText(session.getChatId(), messageId, text, keyboard);
    }

    private InlineKeyboardMarkup productMapAsCallbackQuery(ChatSession session, Locale locale, Steps step) {
        ProductMap productMap = productMapService.findAll(session.getUserId());
        String callbackQueryPrefix = ChatBotUtility.callBackQueryPrefix(BotCommands.PRODUCT_MAP, step.ordinal());
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        for (ProductType p : productMap.getEntries()) {
            var button = keyboardBuilderKit
                    .callbackButton(p.getTitle() + " : " + p.getPrice(), callbackQueryPrefix + p.getId());
            buttons.add(new ArrayList<>());
            buttons.getLast().add(button);
        }
        InlineKeyboardButton cancelButton = keyboardBuilderKit.callbackButton(ButtonKeys.CANCEL, callbackQueryPrefix, locale);
        buttons.add(List.of(cancelButton));
        return keyboardBuilderKit.inlineKeyboard(buttons);
    }

    private String productMapAsMessageText(ProductMap productMap) {
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
        GET_PRODUCT_TYPES,
        PRODUCT_TYPE_LIST_FOR_UPDATE,
        SELECT_PRODUCT_TYPE_TO_UPDATE,
        UPDATE_PRODUCT_TYPE_INPUT,
        PRODUCT_TYPE_LIST_FOR_DELETE,
        SELECT_PRODUCT_TYPE_TO_DELETE
    }

    enum Attributes {
        PRODUCT_TYPE_ID
    }
}

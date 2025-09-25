package org.lab.telegram_bot.domain.command.handlers;

import org.lab.exception.BadRequestCustomException;
import org.lab.model.ProductMap;
import org.lab.model.ProductType;
import org.lab.request.NewProductType;
import org.lab.telegram_bot.controller.advice.TelegramBotExceptionHandler;
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
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import java.util.List;
import java.util.Locale;

@CommandHandler(command = BotCommands.NEW_PRODUCT_TYPE)
public class NewProductTypeHandler extends BotCommandHandler {

    private final ProductMapMvcService productMapService;
    private final KeyboardBuilderKit keyboardBuilderKit;
    private final ChatSessionService chatSessionService;


    @Autowired
    public NewProductTypeHandler(ProductMapMvcService productMapService,
                                 KeyboardBuilderKit keyboardBuilderKit,
                                 MessageSource messageSource,
                                 ChatSessionService chatSessionService) {
        super(messageSource);
        this.productMapService = productMapService;
        this.keyboardBuilderKit = keyboardBuilderKit;
        this.chatSessionService = chatSessionService;
    }


    @Override
    public BotApiMethod<?> handle(Message message, ChatSession session) {
        Locale locale = ChatBotUtility.getLocale(message);
        String messageText = message.getText();
        Steps step = getStep(session);
        return switch (step) {
            case CREATE_NEW_PRODUCT_TYPE -> create(session, locale);
            case INPUT_NEW_PRODUCT_TYPE -> input(session, locale, messageText);
        };
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery, ChatSession session, Locale locale) {
        String text = messageSource.getMessage(TelegramBotExceptionHandler.MessageTextKey.ILLEGAL_ARGUMENT.name(), null, locale);
        return createSendMessage(session.getChatId(), text);
    }


    private SendMessage create(ChatSession session, Locale locale) {
        String text = messageSource.getMessage(BotCommands.NEW_PRODUCT_TYPE.name(), null, locale);
        session.setStep(Steps.INPUT_NEW_PRODUCT_TYPE.ordinal());
        session.setCommand(BotCommands.NEW_PRODUCT_TYPE);
        chatSessionService.save(session);
        return createSendMessage(session.getChatId(), text);
    }

    private BotApiMethod<?> input(ChatSession session, Locale locale, String messageText) {
        String[] values = messageText.split(":");
        if (values.length != 2) {
            throw new BadRequestCustomException("Incorrect data has been entered: " + messageText);
        }
        float price;
        try {
            price = Float.parseFloat(values[1]);
        } catch (NumberFormatException e) {
            throw new BadRequestCustomException("Incorrect price value has been entered: " + values[1]);
        }
        NewProductType newProductType = NewProductType.builder()
                .title(values[0])
                .price(price)
                .build();
        ProductMap productMap = productMapService.create(newProductType, session.getUserId());
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
        CREATE_NEW_PRODUCT_TYPE,
        INPUT_NEW_PRODUCT_TYPE
    }
}

package org.lab.telegram_bot.domain.command.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.lab.exception.BadRequestCustomException;
import org.lab.model.DentalWork;
import org.lab.model.ProductMap;
import org.lab.model.ProductType;
import org.lab.request.NewDentalWork;
import org.lab.telegram_bot.domain.command.BotCommandHandler;
import org.lab.telegram_bot.domain.command.BotCommands;
import org.lab.telegram_bot.domain.command.CommandHandler;
import org.lab.telegram_bot.domain.element.ButtonKeys;
import org.lab.telegram_bot.domain.element.KeyboardBuilderKit;
import org.lab.telegram_bot.domain.session.ChatSession;
import org.lab.telegram_bot.domain.session.ChatSessionService;
import org.lab.telegram_bot.exception.ConfigurationCustomException;
import org.lab.telegram_bot.exception.IncorrectInputException;
import org.lab.telegram_bot.service.DentalWorkMvcService;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;

@CommandHandler(command = BotCommands.NEW_DENTAL_WORK)
public class NewDentalWorkHandler extends BotCommandHandler {

    private final DentalWorkMvcService dentalWorkMvcService;
    private final ProductMapMvcService productMapService;
    private final KeyboardBuilderKit keyboardBuilderKit;
    private final ChatSessionService chatSessionService;
    private final ObjectMapper objectMapper;
    private Consumer<BotApiMethod<?>> executor;


    @Autowired
    public NewDentalWorkHandler(DentalWorkMvcService dentalWorkMvcService,
                                ProductMapMvcService productMapService,
                                KeyboardBuilderKit keyboardBuilderKit,
                                MessageSource messageSource,
                                ChatSessionService chatSessionService,
                                ObjectMapper objectMapper) {
        super(messageSource);
        this.dentalWorkMvcService = dentalWorkMvcService;
        this.productMapService = productMapService;
        this.keyboardBuilderKit = keyboardBuilderKit;
        this.chatSessionService = chatSessionService;
        this.objectMapper = objectMapper;
    }


    @Override
    public BotApiMethod<?> handle(Message message, ChatSession session) {
        Locale locale = ChatBotUtility.getLocale(message);
        String messageText = message.getText();
        Steps steps = getStep(session);
        return switch (steps) {
            case CREATE_NEW_DENTAL_WORK -> create(session, locale);
            case INPUT_NEW_DENTAL_WORK -> input(session, locale, messageText, message.getMessageId());
            case SELECT_PRODUCT_TYPE_FOR_NEW_DENTAL_WORK -> selectProductType(session, locale, messageText, message.getMessageId());
            case INPUT_PRODUCT_QUANTITY_FOR_NEW_DENTAL_WORK -> inputQuantity(session, locale, messageText, message.getMessageId());
            case ADD_PRODUCT_TO_DENTAL_WORK -> addNewProduct(session, locale, messageText, message.getMessageId());
            case NEW_PRODUCT_TO_DENTAL_WORK -> selectNewProduct(session, locale, messageText, message.getMessageId());
            case INPUT_QUANTITY_FOR_NEW_PRODUCT_TO_DENTAL_WORK -> inputQuantityToNewProduct(session, locale, messageText, message.getMessageId());
        };
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery, ChatSession session, Locale locale) {
        String messageText = callbackQuery.getData();
        Steps steps = getStep(session);
        return switch (steps) {
            case CREATE_NEW_DENTAL_WORK -> create(session, locale);
            case INPUT_NEW_DENTAL_WORK -> input(session, locale, messageText, callbackQuery.getMessage().getMessageId());
            case SELECT_PRODUCT_TYPE_FOR_NEW_DENTAL_WORK -> selectProductType(session, locale, messageText, callbackQuery.getMessage().getMessageId());
            case INPUT_PRODUCT_QUANTITY_FOR_NEW_DENTAL_WORK -> inputQuantity(session, locale, messageText, callbackQuery.getMessage().getMessageId());
            case ADD_PRODUCT_TO_DENTAL_WORK -> addNewProduct(session, locale, messageText, callbackQuery.getMessage().getMessageId());
            case NEW_PRODUCT_TO_DENTAL_WORK -> selectNewProduct(session, locale, messageText, callbackQuery.getMessage().getMessageId());
            case INPUT_QUANTITY_FOR_NEW_PRODUCT_TO_DENTAL_WORK -> inputQuantityToNewProduct(session, locale, messageText, callbackQuery.getMessage().getMessageId());
        };
    }

    public void setExecutor(Consumer<BotApiMethod<?>> executor) {
        this.executor = executor;
    }


    private SendMessage create(ChatSession session, Locale locale) {
        String text = messageSource.getMessage(BotCommands.NEW_DENTAL_WORK.name(), null, locale);
        session.setStep(Steps.INPUT_NEW_DENTAL_WORK.ordinal());
        session.setCommand(BotCommands.NEW_DENTAL_WORK);
        chatSessionService.save(session);
        return createSendMessage(session.getChatId(), text);
    }

    private SendMessage input(ChatSession session, Locale locale, String messageText, int messageId) {
        if (executor == null) {
            throw new ConfigurationCustomException("Executor for %s is null".formatted(this.getClass().getSimpleName()));
        }
        String[] values = messageText.split("\n");
        if (values.length < 3) {
            throw new BadRequestCustomException("Incorrect data has been entered: " + messageText);
        }
        LocalDate completeAt = parseLocalDate(values[2].strip());
        NewDentalWork newDentalWork = NewDentalWork.builder()
                .patient(values[0].strip())
                .clinic(values[1].strip())
                .completeAt(completeAt)
                .build();
        if (values.length == 4) {
            newDentalWork.setComment(values[3]);
        }
        String text = messageSource.getMessage(Steps.INPUT_NEW_DENTAL_WORK.name(), null, locale);
        ProductMap productMap = productMapService.findAll(session.getUserId());
        String callbackQueryPrefix = ChatBotUtility.callBackQueryPrefix(BotCommands.NEW_DENTAL_WORK, Steps.SELECT_PRODUCT_TYPE_FOR_NEW_DENTAL_WORK.ordinal());
        InlineKeyboardMarkup keyboardMarkup = productMapAsCallbackQuery(productMap, locale, callbackQueryPrefix);
        session.addAttribute(Attributes.NEW_DENTAL_WORK.name(), newDentalWorkAsString(newDentalWork));
        session.setCommand(BotCommands.NEW_DENTAL_WORK);
        session.setStep(Steps.SELECT_PRODUCT_TYPE_FOR_NEW_DENTAL_WORK.ordinal());
        chatSessionService.save(session);
        executor.accept(deleteMessage(session.getChatId(), messageId));
        executor.accept(deleteMessage(session.getChatId(), messageId - 1));
        return createSendMessage(session.getChatId(), text, keyboardMarkup);
    }

    private EditMessageText selectProductType(ChatSession session, Locale locale, String messageText, int messageId) {
        String[] callbackData = ChatBotUtility.callBackQueryParse(messageText);
        if (callbackData[2].equals(ButtonKeys.CANCEL.name())) {
            String text = messageSource.getMessage(CANCEL_RESPONSE, null, locale);
            session.clear();
            chatSessionService.save(session);
            return editMessageText(session.getChatId(), messageId, text);
        }
        UUID productTypeId = UUID.fromString(callbackData[2]);
        NewDentalWork newDentalWork = stringToNewDentalWork(session.getAttribute(Attributes.NEW_DENTAL_WORK.name()));
        newDentalWork.setProductId(productTypeId);
        session.addAttribute(Attributes.NEW_DENTAL_WORK.name(), newDentalWorkAsString(newDentalWork));
        ProductType productType = productMapService.findById(productTypeId, session.getUserId());
        String text = messageSource.getMessage(Steps.SELECT_PRODUCT_TYPE_FOR_NEW_DENTAL_WORK.name(), new Object[]{productType.getTitle()}, locale);
        session.setStep(Steps.INPUT_PRODUCT_QUANTITY_FOR_NEW_DENTAL_WORK.ordinal());
        session.setCommand(BotCommands.NEW_DENTAL_WORK);
        chatSessionService.save(session);
        return editMessageText(session.getChatId(), messageId, text);
    }

    private SendMessage inputQuantity(ChatSession session, Locale locale, String messageText, int messageId) {
        if (executor == null) {
            throw new ConfigurationCustomException("Executor for %s is null".formatted(this.getClass().getSimpleName()));
        }
        int quantity;
        try {
            quantity = Integer.parseInt(messageText);
        } catch (NumberFormatException e) {
            throw new IncorrectInputException(messageText, e);
        }
        NewDentalWork newDentalWork = stringToNewDentalWork(session.getAttribute(Attributes.NEW_DENTAL_WORK.name()));
        if (newDentalWork == null) {
            throw new IllegalArgumentException("ChatSession attribute '%s' is null".formatted(Attributes.NEW_DENTAL_WORK));
        }
        newDentalWork.setQuantity(quantity);
        DentalWork dentalWork = dentalWorkMvcService.createAndReturnSingle(newDentalWork, session.getUserId());
        String text = dentalWorkAsMessage(dentalWork, locale);
        String buttonLabel = messageSource.getMessage(Steps.ADD_PRODUCT_TO_DENTAL_WORK.name(), null, locale);
        String callbackQueryData = ChatBotUtility.callBackQuery(BotCommands.NEW_DENTAL_WORK, Steps.ADD_PRODUCT_TO_DENTAL_WORK.ordinal(), dentalWork.getId().toString());
        InlineKeyboardButton addProductButton = keyboardBuilderKit.callbackButton(buttonLabel, callbackQueryData);
        InlineKeyboardMarkup inlineKeyboardMarkup = keyboardBuilderKit.inlineKeyboard(List.of(addProductButton));
        session.removeAttribute(Attributes.NEW_DENTAL_WORK.name());
        session.setCommand(BotCommands.NEW_DENTAL_WORK);
        session.setStep(Steps.ADD_PRODUCT_TO_DENTAL_WORK.ordinal());
        chatSessionService.save(session);
        executor.accept(deleteMessage(session.getChatId(), messageId));
        executor.accept(deleteMessage(session.getChatId(), messageId - 1));
        return createSendMessage(session.getChatId(), text, inlineKeyboardMarkup);
    }

    private EditMessageText addNewProduct(ChatSession session, Locale locale, String messageText, int messageId) {
        String[] callbackData = ChatBotUtility.callBackQueryParse(messageText);
        long workId = Long.parseLong(callbackData[2]);
        session.addAttribute(Attributes.DENTAL_WORK_ID.name(), Long.toString(workId));
        String text = messageSource.getMessage(Steps.INPUT_NEW_DENTAL_WORK.name(), null, locale);
        ProductMap productMap = productMapService.findAll(session.getUserId());
        String callbackQueryPrefix = ChatBotUtility.callBackQueryPrefix(BotCommands.NEW_DENTAL_WORK, Steps.NEW_PRODUCT_TO_DENTAL_WORK.ordinal());
        InlineKeyboardMarkup keyboardMarkup = productMapAsCallbackQuery(productMap, locale, callbackQueryPrefix);
        session.setCommand(BotCommands.NEW_DENTAL_WORK);
        session.setStep(Steps.NEW_PRODUCT_TO_DENTAL_WORK.ordinal());
        chatSessionService.save(session);
        return editMessageText(session.getChatId(), messageId, text, keyboardMarkup);
    }

    private EditMessageText selectNewProduct(ChatSession session, Locale locale, String messageText, int messageId) {
        String[] callbackData = ChatBotUtility.callBackQueryParse(messageText);
        if (callbackData[2].equals(ButtonKeys.CANCEL.name())) {
            String text = messageSource.getMessage(CANCEL_RESPONSE, null, locale);
            session.clear();
            chatSessionService.save(session);
            return editMessageText(session.getChatId(), messageId, text);
        }
        UUID productTypeId = UUID.fromString(callbackData[2]);
        ProductType productType = productMapService.findById(productTypeId, session.getUserId());
        String text = messageSource.getMessage(Steps.NEW_PRODUCT_TO_DENTAL_WORK.name(), new Object[]{productType.getTitle()}, locale);
        session.addAttribute(Attributes.PRODUCT_TYPE_ID.name(), productTypeId.toString());
        session.setStep(Steps.INPUT_QUANTITY_FOR_NEW_PRODUCT_TO_DENTAL_WORK.ordinal());
        session.setCommand(BotCommands.NEW_DENTAL_WORK);
        chatSessionService.save(session);
        return editMessageText(session.getChatId(), messageId, text);
    }

    private SendMessage inputQuantityToNewProduct(ChatSession session, Locale locale, String messageText, int messageId) {
        if (executor == null) {
            throw new ConfigurationCustomException("Executor for %s is null".formatted(this.getClass().getSimpleName()));
        }
        String[] values = messageText.split("\n");
        if (values.length != 2) {
            throw new BadRequestCustomException("Incorrect data has been entered: " + messageText);
        }
        int quantity;
        try {
            quantity = Integer.parseInt(values[0]);
        } catch (NumberFormatException e) {
            throw new IncorrectInputException(messageText, e);
        }
        LocalDate completeAt = parseLocalDate(values[1]);
        long workId = Long.parseLong(session.getAttribute(Attributes.DENTAL_WORK_ID.name()));
        UUID productTypeId = UUID.fromString(session.getAttribute(Attributes.PRODUCT_TYPE_ID.name()));
        DentalWork dentalWork = dentalWorkMvcService.addProduct(workId, productTypeId, quantity, completeAt, session.getUserId());
        String text = dentalWorkAsMessage(dentalWork, locale);
        String buttonLabel = messageSource.getMessage(Steps.ADD_PRODUCT_TO_DENTAL_WORK.name(), null, locale);
        String callbackQueryData = ChatBotUtility.callBackQuery(BotCommands.NEW_DENTAL_WORK, Steps.ADD_PRODUCT_TO_DENTAL_WORK.ordinal(), dentalWork.getId().toString());
        InlineKeyboardButton addProductButton = keyboardBuilderKit.callbackButton(buttonLabel, callbackQueryData);
        InlineKeyboardMarkup inlineKeyboardMarkup = keyboardBuilderKit.inlineKeyboard(List.of(addProductButton));
        session.removeAttribute(Attributes.NEW_DENTAL_WORK.name());
        session.addAttribute(Attributes.DENTAL_WORK_ID.name(), dentalWork.getId().toString());
        session.setCommand(BotCommands.NEW_DENTAL_WORK);
        session.setStep(Steps.NEW_PRODUCT_TO_DENTAL_WORK.ordinal());
        chatSessionService.save(session);
        executor.accept(deleteMessage(session.getChatId(), messageId));
        executor.accept(deleteMessage(session.getChatId(), messageId - 1));
        return createSendMessage(session.getChatId(), text, inlineKeyboardMarkup);
    }

    private InlineKeyboardMarkup productMapAsCallbackQuery(ProductMap productMap, Locale locale, String callbackQueryPrefix) {
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

    private LocalDate parseLocalDate(String value) {
        LocalDate completeAt;
        try {
            String dateValue = value;
            if (dateValue.split("\\.").length == 2) {
                dateValue += "." + LocalDate.now().getYear();
            }
            completeAt = LocalDate.parse(dateValue, format);
        } catch (DateTimeParseException e) {
            throw new IncorrectInputException(value, e);
        }
        return completeAt;
    }

    private String newDentalWorkAsString(NewDentalWork newDentalWork) {
        try {
            return objectMapper.writeValueAsString(newDentalWork);
        } catch (JsonProcessingException e) {
            throw new ConfigurationCustomException(e);
        }
    }

    private NewDentalWork stringToNewDentalWork(String json) {
        try {
            return objectMapper.readValue(json, NewDentalWork.class);
        } catch (JsonProcessingException e) {
            throw new ConfigurationCustomException(e);
        }
    }

    private Steps getStep(ChatSession session) {
        return Steps.values()[session.getStep()];
    }


    enum Steps {
        CREATE_NEW_DENTAL_WORK,
        INPUT_NEW_DENTAL_WORK,
        SELECT_PRODUCT_TYPE_FOR_NEW_DENTAL_WORK,
        INPUT_PRODUCT_QUANTITY_FOR_NEW_DENTAL_WORK,
        ADD_PRODUCT_TO_DENTAL_WORK,
        NEW_PRODUCT_TO_DENTAL_WORK,
        INPUT_QUANTITY_FOR_NEW_PRODUCT_TO_DENTAL_WORK
    }

    enum Attributes {
        NEW_DENTAL_WORK,
        DENTAL_WORK_ID,
        PRODUCT_TYPE_ID
    }
}

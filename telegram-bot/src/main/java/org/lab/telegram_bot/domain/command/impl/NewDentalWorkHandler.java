package org.lab.telegram_bot.domain.command.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.lab.exception.BadRequestCustomException;
import org.lab.model.DentalWork;
import org.lab.model.Product;
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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@CommandHandler(command = BotCommands.NEW_DENTAL_WORK)
public class NewDentalWorkHandler extends BotCommandHandler {

    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String DENTAL_WORK_TEMPLATE = "DENTAL_WORK_TEMPLATE";

    private final DentalWorkMvcService dentalWorkMvcService;
    private final ProductMapMvcService productMapService;
    private final KeyboardBuilderKit keyboardBuilderKit;
    private final MessageSource messageSource;
    private final ChatSessionService chatSessionService;
    private final ObjectMapper objectMapper;


    @Autowired
    public NewDentalWorkHandler(DentalWorkMvcService dentalWorkMvcService,
                                ProductMapMvcService productMapService,
                                KeyboardBuilderKit keyboardBuilderKit,
                                MessageSource messageSource,
                                ChatSessionService chatSessionService,
                                ObjectMapper objectMapper) {
        this.dentalWorkMvcService = dentalWorkMvcService;
        this.productMapService = productMapService;
        this.keyboardBuilderKit = keyboardBuilderKit;
        this.messageSource = messageSource;
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
            case INPUT_NEW_DENTAL_WORK -> input(session, locale, messageText);
            case SELECT_PRODUCT_TYPE_FOR_NEW_DENTAL_WORK -> selectProductType(session, locale, messageText, message.getMessageId());
            case INPUT_PRODUCT_QUANTITY_FOR_NEW_DENTAL_WORK -> inputQuantity(session, locale, messageText);
            case ADD_PRODUCT_TO_DENTAL_WORK -> addProduct(session, locale, messageText, message.getMessageId());
        };
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery, ChatSession session, Locale locale) {
        String messageText = callbackQuery.getData();
        Steps steps = getStep(session);
        return switch (steps) {
            case CREATE_NEW_DENTAL_WORK -> create(session, locale);
            case INPUT_NEW_DENTAL_WORK -> input(session, locale, messageText);
            case SELECT_PRODUCT_TYPE_FOR_NEW_DENTAL_WORK -> selectProductType(session, locale, messageText, callbackQuery.getMessage().getMessageId());
            case INPUT_PRODUCT_QUANTITY_FOR_NEW_DENTAL_WORK -> inputQuantity(session, locale, messageText);
            case ADD_PRODUCT_TO_DENTAL_WORK -> addProduct(session, locale, messageText, callbackQuery.getMessage().getMessageId());
        };
    }


    private SendMessage create(ChatSession session, Locale locale) {
        String text = messageSource.getMessage(BotCommands.NEW_DENTAL_WORK.name(), null, locale);
        session.setStep(Steps.INPUT_NEW_DENTAL_WORK.ordinal());
        session.setCommand(BotCommands.NEW_DENTAL_WORK);
        chatSessionService.save(session);
        return createSendMessage(session.getChatId(), text);
    }

    private SendMessage input(ChatSession session, Locale locale, String messageText) {
        String[] values = messageText.split("\n");
        if (values.length < 3) {
            throw new BadRequestCustomException("Incorrect data has been entered: " + messageText);
        }
        LocalDate completeAt;
        try {
            completeAt = LocalDate.parse(values[2], format);
        } catch (DateTimeParseException e) {
            throw new IncorrectInputException(values[2], e);
        }
        NewDentalWork newDentalWork = NewDentalWork.builder()
                .patient(values[0])
                .clinic(values[1])
                .completeAt(completeAt)
                .build();
        if (values.length == 4) {
            newDentalWork.setComment(values[3]);
        }
        String text = messageSource.getMessage(Steps.INPUT_NEW_DENTAL_WORK.name(), null, locale);
        InlineKeyboardMarkup keyboardMarkup = productMapAsCallbackQuery(session, locale);
        session.addAttribute(Attributes.NEW_DENTAL_WORK.name(), newDentalWorkAsString(newDentalWork));
        session.setCommand(BotCommands.NEW_DENTAL_WORK);
        session.setStep(Steps.SELECT_PRODUCT_TYPE_FOR_NEW_DENTAL_WORK.ordinal());
        chatSessionService.save(session);
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
        NewDentalWork newDentalWork = stringToNewDentalWork((String) session.getAttribute(Attributes.NEW_DENTAL_WORK.name()));
        newDentalWork.setProductId(productTypeId);
        session.addAttribute(Attributes.NEW_DENTAL_WORK.name(), newDentalWorkAsString(newDentalWork));
        ProductType productType = productMapService.findById(productTypeId, session.getUserId());
        String text = messageSource.getMessage(Steps.SELECT_PRODUCT_TYPE_FOR_NEW_DENTAL_WORK.name(), new Object[]{productType.getTitle()}, locale);
        session.setStep(Steps.INPUT_PRODUCT_QUANTITY_FOR_NEW_DENTAL_WORK.ordinal());
        session.setCommand(BotCommands.NEW_DENTAL_WORK);
        chatSessionService.save(session);
        return editMessageText(session.getChatId(), messageId, text);
    }

    private SendMessage inputQuantity(ChatSession session, Locale locale, String messageText) {
        int quantity = Integer.parseInt(messageText);
        NewDentalWork newDentalWork = stringToNewDentalWork((String) session.getAttribute(Attributes.NEW_DENTAL_WORK.name()));
        if (newDentalWork == null) {
            throw new IllegalArgumentException("ChatSession attribute '%s' is null".formatted(Attributes.NEW_DENTAL_WORK));
        }
        newDentalWork.setQuantity(quantity);
        DentalWork dentalWork = dentalWorkMvcService.createAndReturnSingle(newDentalWork, session.getUserId());
        session.removeAttribute(Attributes.NEW_DENTAL_WORK.name());
        String text = dentalWorkAsMessage(dentalWork, locale);
        String buttonLabel = messageSource.getMessage(Steps.ADD_PRODUCT_TO_DENTAL_WORK.name(), null, locale);
        String callbackQueryData = ChatBotUtility.callBackQuery(BotCommands.NEW_DENTAL_WORK, Steps.ADD_PRODUCT_TO_DENTAL_WORK.ordinal(), dentalWork.getId().toString());
        InlineKeyboardButton addProductButton = keyboardBuilderKit.callbackButton(buttonLabel, callbackQueryData);
        InlineKeyboardMarkup inlineKeyboardMarkup = keyboardBuilderKit.inlineKeyboard(List.of(addProductButton));
        return createSendMessage(session.getChatId(), text, inlineKeyboardMarkup);
    }

    private EditMessageText addProduct(ChatSession session, Locale locale, String message, int messageId) {
        return null;
    }

    private InlineKeyboardMarkup productMapAsCallbackQuery(ChatSession session, Locale locale) {
        ProductMap productMap = productMapService.findAll(session.getUserId());
        String callbackQueryPrefix = ChatBotUtility.callBackQueryPrefix(BotCommands.NEW_DENTAL_WORK, Steps.SELECT_PRODUCT_TYPE_FOR_NEW_DENTAL_WORK.ordinal());
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

    private String dentalWorkAsMessage(DentalWork dentalWork, Locale locale) {
        String template = messageSource.getMessage(DENTAL_WORK_TEMPLATE, null, locale);
        StringBuilder stringBuilder = new StringBuilder();
        for (Product p : dentalWork.getProducts()) {
            stringBuilder.append('\t')
                    .append(p.getTitle())
                    .append(' ')
                    .append('-')
                    .append(' ')
                    .append(p.getQuantity())
                    .append('\n');
        } stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return template.formatted(
                dentalWork.getPatient(),
                dentalWork.getClinic(),
                stringBuilder.toString(),
                dentalWork.getCompleteAt(),
                dentalWork.getComment() == null ? "" : dentalWork.getComment(),
                dentalWork.countPhoto());
    }

    private Steps getStep(ChatSession session) {
        return Steps.values()[session.getStep()];
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


    enum Steps {
        CREATE_NEW_DENTAL_WORK,
        INPUT_NEW_DENTAL_WORK,
        SELECT_PRODUCT_TYPE_FOR_NEW_DENTAL_WORK,
        INPUT_PRODUCT_QUANTITY_FOR_NEW_DENTAL_WORK,
        ADD_PRODUCT_TO_DENTAL_WORK
    }

    enum Attributes {
        NEW_DENTAL_WORK
    }
}

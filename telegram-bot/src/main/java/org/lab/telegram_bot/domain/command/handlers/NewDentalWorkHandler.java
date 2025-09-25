package org.lab.telegram_bot.domain.command.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.lab.exception.BadRequestCustomException;
import org.lab.model.DentalWork;
import org.lab.model.ProductMap;
import org.lab.model.ProductType;
import org.lab.request.NewDentalWork;
import org.lab.telegram_bot.domain.command.BotCommands;
import org.lab.telegram_bot.domain.command.CommandHandler;
import org.lab.telegram_bot.domain.command.TextKeys;
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
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;

@CommandHandler(command = BotCommands.NEW_DENTAL_WORK)
public class NewDentalWorkHandler extends BotCommandHandler {

    private final DentalWorkMvcService dentalWorkService;
    private final ProductMapMvcService productMapService;
    private final KeyboardBuilderKit keyboardBuilderKit;
    private final ChatSessionService chatSessionService;
    private final ObjectMapper objectMapper;
    private Consumer<BotApiMethod<?>> executor;


    @Autowired
    public NewDentalWorkHandler(DentalWorkMvcService dentalWorkService,
                                ProductMapMvcService productMapService,
                                KeyboardBuilderKit keyboardBuilderKit,
                                MessageSource messageSource,
                                ChatSessionService chatSessionService,
                                ObjectMapper objectMapper) {
        super(messageSource);
        this.dentalWorkService = dentalWorkService;
        this.productMapService = productMapService;
        this.keyboardBuilderKit = keyboardBuilderKit;
        this.chatSessionService = chatSessionService;
        this.objectMapper = objectMapper;
    }


    @Override
    public BotApiMethod<?> handle(Message message, ChatSession session) {
        Locale locale = ChatBotUtility.getLocale(message);
        String messageText = message.getText();
        int messageId = message.getMessageId();
        Steps step = getStep(session);
        return switch (step) {
            case CREATE_NEW -> create(session, locale);
            case INPUT_NEW -> input(session, locale, messageText, messageId);
            case SELECT_PRODUCT_TYPE -> selectProductType(session, locale, messageText, messageId);
            case INPUT_PRODUCT_QUANTITY -> inputQuantity(session, locale, messageText, messageId);
        };
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery, ChatSession session, Locale locale) {
        String messageText = callbackQuery.getData();
        int messageId = callbackQuery.getMessage().getMessageId();
        Steps step = getStep(session);
        return switch (step) {
            case CREATE_NEW -> create(session, locale);
            case INPUT_NEW -> input(session, locale, messageText, messageId);
            case SELECT_PRODUCT_TYPE -> selectProductType(session, locale, messageText, messageId);
            case INPUT_PRODUCT_QUANTITY -> inputQuantity(session, locale, messageText, messageId);
        };
    }

    public void setExecutor(Consumer<BotApiMethod<?>> executor) {
        this.executor = executor;
    }


    private SendMessage create(ChatSession session, Locale locale) {
        String text = messageSource.getMessage(TextKeys.NEW_DENTAL_WORK.name(), null, locale);
        session.setStep(Steps.INPUT_NEW.ordinal());
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
        String text = messageSource.getMessage(TextKeys.SELECT_PRODUCT_TYPE_FOR_WORK.name(), null, locale);
        ProductMap productMap = productMapService.findAll(session.getUserId());
        String callbackQueryPrefix = ChatBotUtility.callBackQueryPrefix(BotCommands.NEW_DENTAL_WORK, Steps.SELECT_PRODUCT_TYPE.ordinal());
        InlineKeyboardMarkup keyboardMarkup = productMapAsCallbackQuery(keyboardBuilderKit, productMap, locale, callbackQueryPrefix);
        session.addAttribute(Attributes.NEW_DENTAL_WORK.name(), newDentalWorkAsString(newDentalWork));
        session.setCommand(BotCommands.NEW_DENTAL_WORK);
        session.setStep(Steps.SELECT_PRODUCT_TYPE.ordinal());
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
        String text = messageSource.getMessage(TextKeys.INPUT_QUANTITY_FOR_PRODUCT.name(), new Object[]{productType.getTitle()}, locale);
        session.setStep(Steps.INPUT_PRODUCT_QUANTITY.ordinal());
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
        DentalWork dentalWork = dentalWorkService.createAndReturnSingle(newDentalWork, session.getUserId());
        session.removeAttribute(NewDentalWorkHandler.Attributes.NEW_DENTAL_WORK.name());
        return viewDentalWork(
                keyboardBuilderKit,
                chatSessionService,
                session,
                locale,
                dentalWork,
                executor,
                messageId, messageId - 1);
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
        CREATE_NEW,
        INPUT_NEW,
        SELECT_PRODUCT_TYPE,
        INPUT_PRODUCT_QUANTITY,
    }

    enum Attributes {
        NEW_DENTAL_WORK
    }
}

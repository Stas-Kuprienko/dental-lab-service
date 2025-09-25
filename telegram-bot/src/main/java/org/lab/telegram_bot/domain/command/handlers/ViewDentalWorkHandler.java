package org.lab.telegram_bot.domain.command.handlers;

import org.lab.exception.BadRequestCustomException;
import org.lab.model.DentalWork;
import org.lab.model.ProductMap;
import org.lab.model.ProductType;
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

@CommandHandler(command = BotCommands.VIEW_DENTAL_WORK)
public class ViewDentalWorkHandler extends BotCommandHandler {

    private final ProductMapMvcService productMapService;
    private final DentalWorkMvcService dentalWorkService;
    private final KeyboardBuilderKit keyboardBuilderKit;
    private final ChatSessionService chatSessionService;
    private Consumer<BotApiMethod<?>> executor;


    @Autowired
    public ViewDentalWorkHandler(MessageSource messageSource,
                                 ProductMapMvcService productMapService,
                                 DentalWorkMvcService dentalWorkService,
                                 KeyboardBuilderKit keyboardBuilderKit,
                                 ChatSessionService chatSessionService) {
        super(messageSource);
        this.productMapService = productMapService;
        this.dentalWorkService = dentalWorkService;
        this.keyboardBuilderKit = keyboardBuilderKit;
        this.chatSessionService = chatSessionService;
    }


    @Override
    public BotApiMethod<?> handle(Message message, ChatSession session) {
        Locale locale = ChatBotUtility.getLocale(message);
        String messageText = message.getText();
        int messageId = message.getMessageId();
        Steps step = getStep(session);
        return switch (step) {
            case ADD_PRODUCT -> addNewProduct(session, locale, messageText, messageId);
            case NEW_PRODUCT -> selectNewProduct(session, locale, messageText, messageId);
            case INPUT_QUANTITY_FOR_NEW_PRODUCT -> inputQuantityToNewProduct(session, locale, messageText, messageId);
        };
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery, ChatSession session, Locale locale) {
        String messageText = callbackQuery.getData();
        int messageId = callbackQuery.getMessage().getMessageId();
        Steps step = getStep(session);
        return switch (step) {
            case ADD_PRODUCT -> addNewProduct(session, locale, messageText, messageId);
            case NEW_PRODUCT -> selectNewProduct(session, locale, messageText, messageId);
            case INPUT_QUANTITY_FOR_NEW_PRODUCT -> inputQuantityToNewProduct(session, locale, messageText, messageId);
        };
    }

    public void setExecutor(Consumer<BotApiMethod<?>> executor) {
        this.executor = executor;
    }


    private EditMessageText addNewProduct(ChatSession session, Locale locale, String messageText, int messageId) {
        String[] callbackData = ChatBotUtility.callBackQueryParse(messageText);
        long workId = Long.parseLong(callbackData[2]);
        session.addAttribute(Attributes.DENTAL_WORK_ID.name(), Long.toString(workId));
        String text = messageSource.getMessage(TextKeys.SELECT_PRODUCT_TYPE_FOR_WORK.name(), null, locale);
        ProductMap productMap = productMapService.findAll(session.getUserId());
        String callbackQueryPrefix = ChatBotUtility.callBackQueryPrefix(BotCommands.VIEW_DENTAL_WORK, Steps.NEW_PRODUCT.ordinal());
        InlineKeyboardMarkup keyboardMarkup = productMapAsCallbackQuery(keyboardBuilderKit, productMap, locale, callbackQueryPrefix);
        session.setCommand(BotCommands.VIEW_DENTAL_WORK);
        session.setStep(Steps.NEW_PRODUCT.ordinal());
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
        String text = messageSource.getMessage(TextKeys.INPUT_QUANTITY_AND_COMPLETION_DATE_FOR_PRODUCT.name(), new Object[]{productType.getTitle()}, locale);
        session.addAttribute(Attributes.PRODUCT_TYPE_ID.name(), productTypeId.toString());
        session.setStep(Steps.INPUT_QUANTITY_FOR_NEW_PRODUCT.ordinal());
        session.setCommand(BotCommands.VIEW_DENTAL_WORK);
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
        DentalWork dentalWork = dentalWorkService.addProduct(workId, productTypeId, quantity, completeAt, session.getUserId());
        return viewDentalWork(
                keyboardBuilderKit,
                chatSessionService,
                session,
                locale,
                dentalWork,
                executor,
                messageId, messageId - 1);
    }

    private Steps getStep(ChatSession session) {
        return Steps.values()[session.getStep()];
    }


    enum Steps {
        ADD_PRODUCT,
        NEW_PRODUCT,
        INPUT_QUANTITY_FOR_NEW_PRODUCT
    }

    enum Attributes {
        DENTAL_WORK_ID,
        PRODUCT_TYPE_ID
    }
}

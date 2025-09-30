package org.lab.telegram_bot.domain.command.handlers;

import org.lab.enums.WorkStatus;
import org.lab.exception.BadRequestCustomException;
import org.lab.model.DentalWork;
import org.lab.model.Product;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
            case DELETE_PRODUCT -> deleteProduct(session, locale, messageText, messageId);
            case SELECT_PRODUCT_TO_DELETE -> selectProductToDelete(session, locale, messageText, messageId);
            case UPDATE_DENTAL_WORK -> update(session, locale, messageText, messageId);
            case UPDATE_PATIENT -> updatePatient(session, locale, messageText, messageId);
            case UPDATE_CLINIC -> updateClinic(session, locale, messageText, messageId);
            case UPDATE_COMPLETION -> updateCompletion(session, locale, messageText, messageId);
            case UPDATE_STATUS -> updateStatus(session, locale, messageText, messageId);
            case UPDATE_COMMENT -> updateComment(session, locale, messageText, messageId);
            case SELECT_PRODUCT_FOR_UPDATE_COMPLETION -> selectProduct(session, locale, messageText, messageId);
            case INPUT_PATIENT -> inputPatient(session, locale, messageText, messageId);
            case INPUT_CLINIC -> inputClinic(session, locale, messageText, messageId);
            case INPUT_STATUS -> inputStatus(session, locale, messageText, messageId);
            case INPUT_COMMENT -> inputComment(session, locale, messageText, messageId);
            case INPUT_COMPLETION -> inputCompletion(session, locale, messageText, messageId);
            case CONFIRM_DELETE_DENTAL_WORK -> confirmDeleteDentalWork(session, locale, messageText, messageId);
            case DELETING_DENTAL_WORK -> deletingDentalWork(session, locale, messageText, messageId);
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
            case DELETE_PRODUCT -> deleteProduct(session, locale, messageText, messageId);
            case SELECT_PRODUCT_TO_DELETE -> selectProductToDelete(session, locale, messageText, messageId);
            case UPDATE_DENTAL_WORK -> update(session, locale, messageText, messageId);
            case UPDATE_PATIENT -> updatePatient(session, locale, messageText, messageId);
            case UPDATE_CLINIC -> updateClinic(session, locale, messageText, messageId);
            case UPDATE_COMPLETION -> updateCompletion(session, locale, messageText, messageId);
            case UPDATE_STATUS -> updateStatus(session, locale, messageText, messageId);
            case UPDATE_COMMENT -> updateComment(session, locale, messageText, messageId);
            case SELECT_PRODUCT_FOR_UPDATE_COMPLETION -> selectProduct(session, locale, messageText, messageId);
            case INPUT_PATIENT -> inputPatient(session, locale, messageText, messageId);
            case INPUT_CLINIC -> inputClinic(session, locale, messageText, messageId);
            case INPUT_STATUS -> inputStatus(session, locale, messageText, messageId);
            case INPUT_COMMENT -> inputComment(session, locale, messageText, messageId);
            case INPUT_COMPLETION -> inputCompletion(session, locale, messageText, messageId);
            case CONFIRM_DELETE_DENTAL_WORK -> confirmDeleteDentalWork(session, locale, messageText, messageId);
            case DELETING_DENTAL_WORK -> deletingDentalWork(session, locale, messageText, messageId);
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

    private EditMessageText deleteProduct(ChatSession session, Locale locale, String messageText, int messageId) {
        String[] callbackData = ChatBotUtility.callBackQueryParse(messageText);
        long workId = Long.parseLong(callbackData[2]);
        session.addAttribute(Attributes.DENTAL_WORK_ID.name(), Long.toString(workId));
        DentalWork dentalWork = dentalWorkService.getById(workId, session.getUserId());
        String text = messageSource.getMessage(TextKeys.SELECT_PRODUCT_FOR_DELETE.name(), null, locale);
        String callbackQueryPrefix = ChatBotUtility.callBackQueryPrefix(BotCommands.VIEW_DENTAL_WORK, Steps.SELECT_PRODUCT_TO_DELETE.ordinal());
        InlineKeyboardMarkup keyboardMarkup = productsAsKeyboard(dentalWork.getProducts(), callbackQueryPrefix, locale);
        session.setCommand(BotCommands.VIEW_DENTAL_WORK);
        session.setStep(Steps.SELECT_PRODUCT_TO_DELETE.ordinal());
        chatSessionService.save(session);
        return editMessageText(session.getChatId(), messageId, text, keyboardMarkup);
    }

    private EditMessageText selectNewProduct(ChatSession session, Locale locale, String messageText, int messageId) {
        String[] callbackData = ChatBotUtility.callBackQueryParse(messageText);
        if (callbackData[2].equals(ButtonKeys.CANCEL.name())) {
            String text = messageSource.getMessage(CANCEL_RESPONSE, null, locale);
            session.reset();
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

    private BotApiMethod<?> selectProductToDelete(ChatSession session, Locale locale, String messageText, int messageId) {
        String[] callbackData = ChatBotUtility.callBackQueryParse(messageText);
        if (callbackData[2].equals(ButtonKeys.CANCEL.name())) {
            String text = messageSource.getMessage(CANCEL_RESPONSE, null, locale);
            session.reset();
            chatSessionService.save(session);
            return editMessageText(session.getChatId(), messageId, text);
        }
        UUID productId = UUID.fromString(callbackData[2]);
        long workId = Long.parseLong(session.getAttribute(Attributes.DENTAL_WORK_ID.name()));
        DentalWork dentalWork = dentalWorkService.deleteProduct(workId, session.getUserId(), productId);
        return viewDentalWork(
                keyboardBuilderKit,
                chatSessionService,
                session,
                locale,
                dentalWork,
                executor,
                messageId);
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

    private EditMessageText update(ChatSession session, Locale locale, String messageText, int messageId) {
        String[] callbackData = ChatBotUtility.callBackQueryParse(messageText);
        long workId = Long.parseLong(callbackData[2]);
        String text = messageSource.getMessage(TextKeys.SELECT_FIELD_TO_UPDATE.name(), null, locale);
        DentalWork dentalWork = dentalWorkService.getById(workId, session.getUserId());
        InlineKeyboardMarkup dentalWorkKeyboard = dentalWorkAsKeyboard(dentalWork, locale);
        session.setCommand(BotCommands.VIEW_DENTAL_WORK);
        chatSessionService.save(session);
        return editMessageText(session.getChatId(), messageId, text, dentalWorkKeyboard);
    }

    private SendMessage updatePatient(ChatSession session, Locale locale, String messageText, int messageId) {
        return updateField(session, locale, messageText, messageId, TextKeys.WorkFields.PATIENT, Steps.INPUT_PATIENT);
    }

    private SendMessage updateClinic(ChatSession session, Locale locale, String messageText, int messageId) {
        return updateField(session, locale, messageText, messageId, TextKeys.WorkFields.CLINIC, Steps.INPUT_CLINIC);
    }

    private SendMessage updateStatus(ChatSession session, Locale locale, String messageText, int messageId) {
        String[] callbackData = ChatBotUtility.callBackQueryParse(messageText);
        long workId = Long.parseLong(callbackData[2]);
        session.addAttribute(Attributes.DENTAL_WORK_ID.name(), Long.toString(workId));
        session.addAttribute(Attributes.MESSAGE_ID_TO_DELETE.name(), Integer.toString(messageId));
        String field = messageSource.getMessage(TextKeys.WorkFields.STATUS.name(), null, locale);
        String text = messageSource.getMessage(TextKeys.INPUT_NEW_VALUE_FOR_FIELD.name(), new Object[]{field}, locale);
        InlineKeyboardMarkup statuses = statusesAsKeyboard(locale);
        session.setCommand(BotCommands.VIEW_DENTAL_WORK);
        session.setStep(Steps.UPDATE_STATUS.ordinal());
        chatSessionService.save(session);
        return createSendMessage(session.getChatId(), text, statuses);
    }

    private SendMessage updateCompletion(ChatSession session, Locale locale, String messageText, int messageId) {
        String[] callbackData = ChatBotUtility.callBackQueryParse(messageText);
        long workId = Long.parseLong(callbackData[2]);
        DentalWork dentalWork = dentalWorkService.getById(workId, session.getUserId());
        String callbackPrefix = ChatBotUtility.callBackQueryPrefix(BotCommands.VIEW_DENTAL_WORK, Steps.SELECT_PRODUCT_FOR_UPDATE_COMPLETION.ordinal());
        InlineKeyboardMarkup keyboardMarkup = productsAsKeyboard(dentalWork.getProducts(), callbackPrefix, locale);
        session.addAttribute(Attributes.DENTAL_WORK_ID.name(), Long.toString(workId));
        session.addAttribute(Attributes.MESSAGE_ID_TO_DELETE.name(), Integer.toString(messageId));
        String text = messageSource.getMessage(TextKeys.SELECT_PRODUCT_FOR_UPDATE_COMPLETION.name(), null, locale);
        session.setCommand(BotCommands.VIEW_DENTAL_WORK);
        session.setStep(Steps.SELECT_PRODUCT_FOR_UPDATE_COMPLETION.ordinal());
        chatSessionService.save(session);
        return createSendMessage(session.getChatId(), text, keyboardMarkup);
    }

    private EditMessageText selectProduct(ChatSession session, Locale locale, String messageText, int messageId) {
        String[] callbackData = ChatBotUtility.callBackQueryParse(messageText);
        UUID productId = UUID.fromString(callbackData[2]);
        session.addAttribute(Attributes.PRODUCT_ID.name(), productId.toString());
        String text = messageSource.getMessage(TextKeys.INPUT_NEW_COMPLETION_DATE_FOR_PRODUCT.name(), null, locale);
        session.setCommand(BotCommands.VIEW_DENTAL_WORK);
        session.setStep(Steps.INPUT_COMPLETION.ordinal());
        chatSessionService.save(session);
        return editMessageText(session.getChatId(), messageId, text);
    }

    private SendMessage updateComment(ChatSession session, Locale locale, String messageText, int messageId) {
        return updateField(session, locale, messageText, messageId, TextKeys.WorkFields.COMMENT, Steps.INPUT_COMMENT);
    }

    private SendMessage updateField(ChatSession session, Locale locale, String messageText, int messageId, TextKeys.WorkFields workField, Steps step) {
        String[] callbackData = ChatBotUtility.callBackQueryParse(messageText);
        long workId = Long.parseLong(callbackData[2]);
        session.addAttribute(Attributes.DENTAL_WORK_ID.name(), Long.toString(workId));
        session.addAttribute(Attributes.MESSAGE_ID_TO_DELETE.name(), Integer.toString(messageId));
        String field = messageSource.getMessage(workField.name(), null, locale);
        String text = messageSource.getMessage(TextKeys.INPUT_NEW_VALUE_FOR_FIELD.name(), new Object[]{field}, locale);
        session.setCommand(BotCommands.VIEW_DENTAL_WORK);
        session.setStep(step.ordinal());
        chatSessionService.save(session);
        return createSendMessage(session.getChatId(), text);
    }

    private SendMessage inputPatient(ChatSession session, Locale locale, String messageText, int messageId) {
        return inputField(session, locale, messageText, messageId, TextKeys.WorkFields.PATIENT);
    }

    private SendMessage inputClinic(ChatSession session, Locale locale, String messageText, int messageId) {
        return inputField(session, locale, messageText, messageId, TextKeys.WorkFields.CLINIC);
    }

    private SendMessage inputStatus(ChatSession session, Locale locale, String messageText, int messageId) {
        long workId = Long.parseLong(session.getAttribute(Attributes.DENTAL_WORK_ID.name()));
        DentalWork dentalWork = dentalWorkService.getById(workId, session.getUserId());
        String messageData = ChatBotUtility.callBackQueryParse(messageText)[2];
        dentalWork.setStatus(WorkStatus.valueOf(messageData.toUpperCase()));
        dentalWork = dentalWorkService.update(dentalWork, session.getUserId());
        int messageToDelete = Integer.parseInt(session.getAttribute(DentalWorksHandler.Attributes.MESSAGE_ID_TO_DELETE.name()));
        return viewDentalWork(
                keyboardBuilderKit,
                chatSessionService,
                session,
                locale,
                dentalWork,
                executor,
                messageId, messageToDelete);
    }

    private SendMessage inputComment(ChatSession session, Locale locale, String messageText, int messageId) {
        return inputField(session, locale, messageText, messageId, TextKeys.WorkFields.COMMENT);
    }

    private SendMessage inputField(ChatSession session, Locale locale, String messageText, int messageId, TextKeys.WorkFields field) {
        long workId = Long.parseLong(session.getAttribute(Attributes.DENTAL_WORK_ID.name()));
        DentalWork dentalWork = dentalWorkService.getById(workId, session.getUserId());
        switch (field) {
            case PATIENT -> dentalWork.setPatient(messageText.strip());
            case CLINIC -> dentalWork.setClinic(messageText.strip());
            case STATUS -> {
                String messageData = ChatBotUtility.callBackQueryParse(messageText)[2];
                dentalWork.setStatus(WorkStatus.valueOf(messageData.toUpperCase()));
            }
            case COMMENT -> dentalWork.setComment(messageText.strip());
            default -> throw new ConfigurationCustomException("Argument 'field' is not expected: " + field);
        }
        dentalWork = dentalWorkService.update(dentalWork, session.getUserId());
        int messageToDelete = Integer.parseInt(session.getAttribute(DentalWorksHandler.Attributes.MESSAGE_ID_TO_DELETE.name()));
        return viewDentalWork(
                keyboardBuilderKit,
                chatSessionService,
                session,
                locale,
                dentalWork,
                executor,
                messageId, messageId - 1, messageToDelete);
    }

    private SendMessage inputCompletion(ChatSession session, Locale locale, String messageText, int messageId) {
        long workId = Long.parseLong(session.getAttribute(Attributes.DENTAL_WORK_ID.name()));
        UUID productId = UUID.fromString(session.getAttribute(Attributes.PRODUCT_ID.name()));
        LocalDate completeAt = parseLocalDate(messageText);
        DentalWork dentalWork = dentalWorkService.updateProductCompletion(workId, session.getUserId(), productId, completeAt);
        int messageToDelete = Integer.parseInt(session.getAttribute(DentalWorksHandler.Attributes.MESSAGE_ID_TO_DELETE.name()));
        return viewDentalWork(
                keyboardBuilderKit,
                chatSessionService,
                session,
                locale,
                dentalWork,
                executor,
                messageId, messageId - 1, messageToDelete);
    }

    private SendMessage confirmDeleteDentalWork(ChatSession session, Locale locale, String messageText, int messageId) {
        String[] callbackData = ChatBotUtility.callBackQueryParse(messageText);
        long workId = Long.parseLong(callbackData[2]);
        session.addAttribute(Attributes.DENTAL_WORK_ID.name(), Long.toString(workId));
        session.addAttribute(Attributes.MESSAGE_ID_TO_DELETE.name(), Integer.toString(messageId));
        String text = messageSource.getMessage(TextKeys.DELETE_DENTAL_WORK.name(), null, locale);
        String callbackPrefix = ChatBotUtility.callBackQueryPrefix(BotCommands.VIEW_DENTAL_WORK, Steps.DELETING_DENTAL_WORK.ordinal());
        InlineKeyboardButton yesButton = keyboardBuilderKit.callbackButton(ButtonKeys.YES, callbackPrefix, locale);
        InlineKeyboardButton noButton = keyboardBuilderKit.callbackButton(ButtonKeys.NO, callbackPrefix, locale);
        InlineKeyboardMarkup keyboardMarkup = keyboardBuilderKit.inlineKeyboard(List.of(yesButton, noButton));
        session.setCommand(BotCommands.VIEW_DENTAL_WORK);
        session.setStep(Steps.DELETING_DENTAL_WORK.ordinal());
        chatSessionService.save(session);
        return createSendMessage(session.getChatId(), text, keyboardMarkup);
    }

    private BotApiMethod<?> deletingDentalWork(ChatSession session, Locale locale, String messageText, int messageId) {
        String[] callbackData = ChatBotUtility.callBackQueryParse(messageText);
        ButtonKeys response = ButtonKeys.valueOf(callbackData[2]);
        long workId = Long.parseLong(session.getAttribute(Attributes.DENTAL_WORK_ID.name()));
        int messageIdToDelete = Integer.parseInt(session.getAttribute(Attributes.MESSAGE_ID_TO_DELETE.name()));
        switch (response) {
            case NO -> {
                DentalWork dentalWork = dentalWorkService.getById(workId, session.getUserId());
                return viewDentalWork(
                        keyboardBuilderKit,
                        chatSessionService,
                        session,
                        locale,
                        dentalWork,
                        executor,
                        messageIdToDelete, messageId);
            }
            case YES -> {
                dentalWorkService.delete(workId, session.getUserId());
                String text = messageSource.getMessage(TextKeys.DENTAL_WORK_IS_DELETED.name(), null, locale);
                session.reset();
                chatSessionService.save(session);
                executor.accept(deleteMessage(session.getChatId(), messageIdToDelete));
                return editMessageText(session.getChatId(), messageId, text);
            }
            default -> throw new BadRequestCustomException("Not expected response: " + response);
        }
    }

    private InlineKeyboardMarkup dentalWorkAsKeyboard(DentalWork dentalWork, Locale locale) {
        String workId = dentalWork.getId().toString();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        //create 'patient' button
        Object[] args = new Object[]{dentalWork.getPatient()};
        String buttonLabel = messageSource.getMessage(Steps.UPDATE_PATIENT.name(), args, locale);
        String callbackData = ChatBotUtility.callBackQuery(BotCommands.VIEW_DENTAL_WORK, Steps.UPDATE_PATIENT.ordinal(), workId);
        buttons.add(List.of(keyboardBuilderKit.callbackButton(buttonLabel, callbackData)));
        //create 'clinic' button
        args[0] = dentalWork.getClinic();
        buttonLabel = messageSource.getMessage(Steps.UPDATE_CLINIC.name(), args, locale);
        callbackData = ChatBotUtility.callBackQuery(BotCommands.VIEW_DENTAL_WORK, Steps.UPDATE_CLINIC.ordinal(), workId);
        buttons.add(List.of(keyboardBuilderKit.callbackButton(buttonLabel, callbackData)));
        //create 'status' button
        String statusForLocal = messageSource.getMessage(dentalWork.getStatus().name(), null, locale);
        args[0] = statusForLocal;
        buttonLabel = messageSource.getMessage(Steps.UPDATE_STATUS.name(), args, locale);
        callbackData = ChatBotUtility.callBackQuery(BotCommands.VIEW_DENTAL_WORK, Steps.UPDATE_STATUS.ordinal(), workId);
        buttons.add(List.of(keyboardBuilderKit.callbackButton(buttonLabel, callbackData)));
        //create 'completion' button
        args[0] = dentalWork.getCompleteAt().format(format);
        buttonLabel = messageSource.getMessage(Steps.UPDATE_COMPLETION.name(), args, locale);
        callbackData = ChatBotUtility.callBackQuery(BotCommands.VIEW_DENTAL_WORK, Steps.UPDATE_COMPLETION.ordinal(), workId);
        buttons.add(List.of(keyboardBuilderKit.callbackButton(buttonLabel, callbackData)));
        //create 'comment' button
        args[0] = dentalWork.getComment() == null ? "" : dentalWork.getComment();
        buttonLabel = messageSource.getMessage(Steps.UPDATE_COMMENT.name(), args, locale);
        callbackData = ChatBotUtility.callBackQuery(BotCommands.VIEW_DENTAL_WORK, Steps.UPDATE_COMMENT.ordinal(), workId);
        buttons.add(List.of(keyboardBuilderKit.callbackButton(buttonLabel, callbackData)));
        //build keyboard
        return keyboardBuilderKit.inlineKeyboard(buttons);
    }

    private InlineKeyboardMarkup statusesAsKeyboard(Locale locale) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        int step = Steps.INPUT_STATUS.ordinal();
        String buttonLabel;
        String callbackData;
        for (WorkStatus status : WorkStatus.values()) {
            buttonLabel = messageSource.getMessage(status.name(), null, locale);
            callbackData = ChatBotUtility.callBackQuery(BotCommands.VIEW_DENTAL_WORK, step, status.name());
            buttons.add(List.of(keyboardBuilderKit.callbackButton(buttonLabel, callbackData)));
        }
        String callbackQueryPrefix = ChatBotUtility.callBackQueryPrefix(BotCommands.CLEAR, 0);
        buttons.add(List.of(keyboardBuilderKit.callbackButton(ButtonKeys.CANCEL, callbackQueryPrefix, locale)));
        return keyboardBuilderKit.inlineKeyboard(buttons);
    }

    private InlineKeyboardMarkup productsAsKeyboard(List<Product> products, String callbackQueryPrefix, Locale locale) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        for (Product p : products) {
            var button = keyboardBuilderKit
                    .callbackButton(p.getTitle() + " - " + p.getQuantity(), callbackQueryPrefix + p.getId());
            buttons.add(new ArrayList<>());
            buttons.getLast().add(button);
        }
        InlineKeyboardButton cancelButton = keyboardBuilderKit.callbackButton(ButtonKeys.CANCEL, ChatBotUtility.callBackQueryPrefix(BotCommands.CLEAR, 0), locale);
        buttons.add(List.of(cancelButton));
        return keyboardBuilderKit.inlineKeyboard(buttons);
    }

    private Steps getStep(ChatSession session) {
        return Steps.values()[session.getStep()];
    }


    enum Steps {
        ADD_PRODUCT,
        NEW_PRODUCT,
        INPUT_QUANTITY_FOR_NEW_PRODUCT,
        DELETE_PRODUCT,
        SELECT_PRODUCT_TO_DELETE,
        UPDATE_DENTAL_WORK,
        UPDATE_PATIENT,
        UPDATE_CLINIC,
        UPDATE_STATUS,
        UPDATE_COMPLETION,
        UPDATE_COMMENT,
        INPUT_PATIENT,
        INPUT_CLINIC,
        INPUT_STATUS,
        SELECT_PRODUCT_FOR_UPDATE_COMPLETION,
        INPUT_COMPLETION,
        INPUT_COMMENT,
        CONFIRM_DELETE_DENTAL_WORK,
        DELETING_DENTAL_WORK
    }

    enum Attributes {
        DENTAL_WORK_ID,
        PRODUCT_TYPE_ID,
        PRODUCT_ID,
        MESSAGE_ID_TO_DELETE
    }
}

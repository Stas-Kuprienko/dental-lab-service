package org.lab.telegram_bot.domain.command.handlers;

import org.lab.model.DentalWork;
import org.lab.model.Product;
import org.lab.model.ProductMap;
import org.lab.model.ProductType;
import org.lab.telegram_bot.domain.command.BotCommands;
import org.lab.telegram_bot.domain.command.TextKeys;
import org.lab.telegram_bot.domain.element.ButtonKeys;
import org.lab.telegram_bot.domain.element.KeyboardBuilderKit;
import org.lab.telegram_bot.domain.session.ChatSession;
import org.lab.telegram_bot.domain.session.ChatSessionService;
import org.lab.telegram_bot.exception.IncorrectInputException;
import org.lab.telegram_bot.utils.ChatBotUtility;
import org.springframework.context.MessageSource;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public abstract class BotCommandHandler {

    protected static final DateTimeFormatter format = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    protected static final String DENTAL_WORK_TEMPLATE = "DENTAL_WORK_TEMPLATE";
    protected static final String CANCEL_RESPONSE = "CANCEL_RESPONSE";
    protected static final String WORK_LIST_TEMPLATE = "WORK_LIST_TEMPLATE";
    protected static final String ITEM_DELIMITER = "\n******************\n";

    protected final MessageSource messageSource;


    protected BotCommandHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }


    public abstract BotApiMethod<?> handle(Message message, ChatSession session);

    public abstract BotApiMethod<?> handle(CallbackQuery callbackQuery, ChatSession session, Locale locale);

    protected SendMessage createSendMessage(long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        return sendMessage;
    }

    protected SendMessage createSendMessage(long chatId, String text, ReplyKeyboard keyboard) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(keyboard);
        return sendMessage;
    }

    protected EditMessageText editMessageText(long chatId, int messageId, String text) {
        EditMessageText messageText = new EditMessageText();
        messageText.setChatId(chatId);
        messageText.setMessageId(messageId);
        messageText.setText(text);
        return messageText;
    }

    protected EditMessageText editMessageText(long chatId, int messageId, String text, InlineKeyboardMarkup keyboard) {
        EditMessageText messageText = new EditMessageText();
        messageText.setChatId(chatId);
        messageText.setMessageId(messageId);
        messageText.setText(text);
        messageText.setReplyMarkup(keyboard);
        return messageText;
    }

    protected DeleteMessage deleteMessage(long chatId, int messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);
        return deleteMessage;
    }

    protected String workListToMessage(List<DentalWork> dentalWorks, Locale locale) {
        if (dentalWorks == null || dentalWorks.isEmpty()) {
            return messageSource.getMessage(TextKeys.EMPTY.name(), null, locale);
        }
        String template = messageSource.getMessage(WORK_LIST_TEMPLATE, null, locale);
        StringBuilder workStringBuilder = new StringBuilder();
        StringBuilder productStringBuilder = new StringBuilder();
        for (DentalWork dw : dentalWorks) {
            for (Product p : dw.getProducts()) {
                productStringBuilder.append('\t')
                        .append(p.getTitle())
                        .append(' ')
                        .append('-')
                        .append(' ')
                        .append(p.getQuantity())
                        .append('\n');
            }
            productStringBuilder.deleteCharAt(productStringBuilder.length() - 1);
            String item = template.formatted(
                    dw.getId(),
                    dw.getPatient(),
                    dw.getClinic(),
                    productStringBuilder.toString(),
                    dw.getCompleteAt().format(format));
            workStringBuilder.append(item)
                    .append('\n')
                    .append(ITEM_DELIMITER)
                    .append('\n');
            productStringBuilder.setLength(0);
        }
        return workStringBuilder.toString();
    }

    protected SendMessage viewDentalWork(KeyboardBuilderKit keyboardBuilderKit,
                                         ChatSessionService chatSessionService,
                                         ChatSession session,
                                         Locale locale,
                                         DentalWork dentalWork,
                                         Consumer<BotApiMethod<?>> executor,
                                         int... messageIdToDelete) {
        String text = dentalWorkAsMessage(dentalWork, locale);
        String workId = dentalWork.getId().toString();
        //create 'update' button
        String buttonLabel = messageSource.getMessage(ButtonKeys.UPDATE.name(), null, locale);
        String callbackQueryData = ChatBotUtility.callBackQuery(BotCommands.VIEW_DENTAL_WORK, ViewDentalWorkHandler.Steps.UPDATE_DENTAL_WORK.ordinal(), workId);
        InlineKeyboardButton updateButton = keyboardBuilderKit.callbackButton(buttonLabel, callbackQueryData);
        //create 'add photo' button
        buttonLabel = messageSource.getMessage(ButtonKeys.ADD_PHOTO.name(), null, locale);
        callbackQueryData = ChatBotUtility.callBackQuery(BotCommands.PHOTO_FILES, PhotoFilesCommandHandler.Steps.START_UPLOADING.ordinal(), workId);
        InlineKeyboardButton addPhotoButton = keyboardBuilderKit.callbackButton(buttonLabel, callbackQueryData);
        //create 'open photo' button
        buttonLabel = messageSource.getMessage(ButtonKeys.OPEN_PHOTO.name(), null, locale);
        callbackQueryData = ChatBotUtility.callBackQuery(BotCommands.PHOTO_FILES, PhotoFilesCommandHandler.Steps.OPEN_PHOTOS.ordinal(), workId);
        InlineKeyboardButton openPhotoButton = keyboardBuilderKit.callbackButton(buttonLabel, callbackQueryData);
        //create 'delete work' button
        buttonLabel = messageSource.getMessage(ButtonKeys.DELETE.name(), null, locale);
        callbackQueryData = ChatBotUtility.callBackQuery(BotCommands.VIEW_DENTAL_WORK, ViewDentalWorkHandler.Steps.CONFIRM_DELETE_DENTAL_WORK.ordinal(), workId);
        InlineKeyboardButton deleteWorkButton = keyboardBuilderKit.callbackButton(buttonLabel, callbackQueryData);

        InlineKeyboardMarkup inlineKeyboardMarkup = keyboardBuilderKit.inlineKeyboard(
                List.of(updateButton),
                List.of(addPhotoButton),
                List.of(openPhotoButton),
                List.of(deleteWorkButton));
        session.setCommand(BotCommands.VIEW_DENTAL_WORK);
        session.clearAttributes();
        chatSessionService.save(session);
        for (int id : messageIdToDelete) {
            executor.accept(deleteMessage(session.getChatId(), id));
        }
        return createSendMessage(session.getChatId(), text, inlineKeyboardMarkup);
    }

    protected String dentalWorkAsMessage(DentalWork dentalWork, Locale locale) {
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
        String status = messageSource.getMessage(dentalWork.getStatus().name(), null, locale);
        return template.formatted(
                dentalWork.getPatient(),
                dentalWork.getClinic(),
                stringBuilder.toString(),
                dentalWork.getCompleteAt().format(format),
                status,
                dentalWork.getComment() == null ? "" : dentalWork.getComment(),
                dentalWork.countPhoto());
    }

    protected InlineKeyboardMarkup productMapAsCallbackQuery(KeyboardBuilderKit keyboardBuilderKit,
                                                             ProductMap productMap,
                                                             Locale locale,
                                                             String callbackQueryPrefix) {
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

    protected LocalDate parseLocalDate(String value) {
        LocalDate completeAt;
        try {
            String dateValue = value.strip();
            if (dateValue.split("\\.").length == 2) {
                dateValue += "." + LocalDate.now().getYear();
            }
            completeAt = LocalDate.parse(dateValue, format);
        } catch (DateTimeParseException e) {
            throw new IncorrectInputException(value, e);
        }
        return completeAt;
    }
}

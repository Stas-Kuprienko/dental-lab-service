package org.lab.telegram_bot.domain.command;

import org.lab.model.DentalWork;
import org.lab.model.Product;
import org.lab.telegram_bot.domain.session.ChatSession;
import org.springframework.context.MessageSource;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public abstract class BotCommandHandler {

    protected static final DateTimeFormatter format = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    protected static final String DENTAL_WORK_TEMPLATE = "DENTAL_WORK_TEMPLATE";
    protected static final String CANCEL_RESPONSE = "CANCEL_RESPONSE";

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
        return template.formatted(
                dentalWork.getPatient(),
                dentalWork.getClinic(),
                stringBuilder.toString(),
                dentalWork.getCompleteAt().format(format),
                dentalWork.getComment() == null ? "" : dentalWork.getComment(),
                dentalWork.countPhoto());
    }
}

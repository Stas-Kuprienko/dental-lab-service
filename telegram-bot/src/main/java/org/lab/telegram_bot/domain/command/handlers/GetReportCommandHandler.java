package org.lab.telegram_bot.domain.command.handlers;

import org.lab.exception.BadRequestCustomException;
import org.lab.telegram_bot.domain.command.BotCommands;
import org.lab.telegram_bot.domain.command.CommandHandler;
import org.lab.telegram_bot.domain.command.TextKeys;
import org.lab.telegram_bot.domain.element.ButtonKeys;
import org.lab.telegram_bot.domain.element.KeyboardBuilderKit;
import org.lab.telegram_bot.domain.session.ChatSession;
import org.lab.telegram_bot.domain.session.ChatSessionService;
import org.lab.telegram_bot.exception.ConfigurationCustomException;
import org.lab.telegram_bot.service.DentalLabRestClientWrapper;
import org.lab.telegram_bot.service.ReportServiceWrapper;
import org.lab.telegram_bot.utils.ChatBotUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

@CommandHandler(command = BotCommands.GET_REPORT)
public class GetReportCommandHandler extends BotCommandHandler {

    private final ReportServiceWrapper reportService;
    private final KeyboardBuilderKit keyboardBuilderKit;
    private final ChatSessionService chatSessionService;
    private Consumer<SendDocument> sendDocumentExecutor;


    @Autowired
    public GetReportCommandHandler(MessageSource messageSource,
                                   DentalLabRestClientWrapper dentalLabRestClient,
                                   KeyboardBuilderKit keyboardBuilderKit,
                                   ChatSessionService chatSessionService) {
        super(messageSource);
        this.reportService = dentalLabRestClient.REPORTS;
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
            case SELECT_MONTH -> selectMonth(session, locale);
            case INPUT_MONTH -> inputMonth(session, locale, messageText, messageId);
            case ANOTHER_MONTH -> anotherMonth(session, messageText, messageId);
        };
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery, ChatSession session, Locale locale) {
        String messageText = callbackQuery.getData();
        int messageId = callbackQuery.getMessage().getMessageId();
        Steps step = getStep(session);
        return switch (step) {
            case SELECT_MONTH -> selectMonth(session, locale);
            case INPUT_MONTH -> inputMonth(session, locale, messageText, messageId);
            case ANOTHER_MONTH -> anotherMonth(session, messageText, messageId);
        };
    }

    public void setSendDocumentExecutor(Consumer<SendDocument> sendDocumentExecutor) {
        this.sendDocumentExecutor = sendDocumentExecutor;
    }


    private SendMessage selectMonth(ChatSession session, Locale locale) {
        String text = messageSource.getMessage(TextKeys.SELECT_MONTH_FOR_REPORT.name(), null, locale);
        InlineKeyboardMarkup keyboardMarkup = monthsAsKeyboard(locale);
        session.setCommand(BotCommands.GET_REPORT);
        session.setStep(Steps.INPUT_MONTH.ordinal());
        chatSessionService.save(session);
        return createSendMessage(session.getChatId(), text, keyboardMarkup);
    }

    private BotApiMethod<?> inputMonth(ChatSession session, Locale locale, String messageText, int messageId) {
        if (sendDocumentExecutor == null) {
            throw new ConfigurationCustomException("Executor for %s is null".formatted(this.getClass().getSimpleName()));
        }
        String[] callbackData = ChatBotUtility.callBackQueryParse(messageText);
        Month month;
        try {
            month = Month.valueOf(callbackData[2]);
            YearMonth yearMonth = YearMonth.now().withMonth(month.getValue());
            return sendReport(session, yearMonth, messageId);
        } catch (IllegalArgumentException ignored) {
            ButtonKeys buttonKeys = ButtonKeys.valueOf(callbackData[2]);
            if (buttonKeys == ButtonKeys.ANOTHER_MONTH) {
                String text = messageSource.getMessage(TextKeys.INPUT_ANOTHER_MONTH_FOR_REPORT.name(), null, locale);
                session.setCommand(BotCommands.GET_REPORT);
                session.setStep(Steps.ANOTHER_MONTH.ordinal());
                chatSessionService.save(session);
                return editMessageText(session.getChatId(), messageId, text);
            } else {
                throw new BadRequestCustomException("Unexpected response: " + callbackData[2]);
            }
        }
    }

    private BotApiMethod<?> anotherMonth(ChatSession session, String messageText, int messageId) {
        String[] dateValue = messageText.strip().split("\\.");
        if (dateValue.length > 2) {
            throw new BadRequestCustomException("Incorrect value inputted: " + messageText);
        }
        if (dateValue.length == 1) {
            messageText += "." + LocalDate.now().getYear();
        }
        YearMonth yearMonth = YearMonth.parse(messageText, DateTimeFormatter.ofPattern("MM.yyyy"));
        return sendReport(session, yearMonth, messageId);
    }

    private InlineKeyboardMarkup monthsAsKeyboard(Locale locale) {
        LocalDate now = LocalDate.now();
        Month current = now.getMonth();
        Month previous = now.minusMonths(1).getMonth();
        String callbackPrefix = ChatBotUtility.callBackQueryPrefix(BotCommands.GET_REPORT, Steps.INPUT_MONTH.ordinal());
        //current month button
        String currentLabel = current.getDisplayName(TextStyle.FULL_STANDALONE, locale).toUpperCase();
        InlineKeyboardButton currentButton = keyboardBuilderKit
                .callbackButton(currentLabel, callbackPrefix + current.name());
        //previous month button
        String previousLabel = previous.getDisplayName(TextStyle.FULL_STANDALONE, locale).toUpperCase();
        InlineKeyboardButton previousButton = keyboardBuilderKit
                .callbackButton(previousLabel, callbackPrefix + previous.name());
        //another month button
        String anotherMonthLabel = messageSource.getMessage(ButtonKeys.ANOTHER_MONTH.name(), null, locale);
        InlineKeyboardButton anotherMonthButton = keyboardBuilderKit
                .callbackButton(anotherMonthLabel, callbackPrefix + ButtonKeys.ANOTHER_MONTH.name());

        return keyboardBuilderKit.inlineKeyboard(
                List.of(currentButton),
                List.of(previousButton),
                List.of(anotherMonthButton));
    }

    private DeleteMessage sendReport(ChatSession session, YearMonth yearMonth, int messageIdToDelete) {
        byte[] fileBytes = reportService.downloadWorkReport(yearMonth.getYear(), yearMonth.getMonthValue(), session.getUserId());
        String fileName = yearMonth.getMonth().name() + '_' + yearMonth.getYear();
        session.reset();
        chatSessionService.save(session);
        SendDocument sendDocument = buildFile(fileBytes, fileName, session.getChatId());
        sendDocumentExecutor.accept(sendDocument);
        return deleteMessage(session.getChatId(), messageIdToDelete);
    }

    private SendDocument buildFile(byte[] fileBytes, String fileName, long chatId) {
        fileName += ".xlsx";
        InputStream inputStream = new ByteArrayInputStream(fileBytes);
        InputFile inputFile = new InputFile(inputStream, fileName);
        return SendDocument.builder()
                .chatId(chatId)
                .document(inputFile)
                .build();
    }

    private Steps getStep(ChatSession session) {
        return Steps.values()[session.getStep()];
    }


    enum Steps {
        SELECT_MONTH,
        INPUT_MONTH,
        ANOTHER_MONTH,
    }
}

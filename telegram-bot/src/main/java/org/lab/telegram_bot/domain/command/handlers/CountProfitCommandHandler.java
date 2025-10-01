package org.lab.telegram_bot.domain.command.handlers;

import org.lab.exception.BadRequestCustomException;
import org.lab.model.ProfitRecord;
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
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

@CommandHandler(command = BotCommands.COUNT_PROFIT)
public class CountProfitCommandHandler extends BotCommandHandler {

    private final ReportServiceWrapper reportService;
    private final ChatSessionService chatSessionService;
    private final KeyboardBuilderKit keyboardBuilderKit;
    private Consumer<BotApiMethod<?>> executor;


    @Autowired
    public CountProfitCommandHandler(MessageSource messageSource,
                                     DentalLabRestClientWrapper dentalLabRestClient,
                                     ChatSessionService chatSessionService,
                                     KeyboardBuilderKit keyboardBuilderKit) {
        super(messageSource);
        this.reportService = dentalLabRestClient.REPORTS;
        this.chatSessionService = chatSessionService;
        this.keyboardBuilderKit = keyboardBuilderKit;
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
            case ANOTHER_MONTH -> anotherMonth(session, locale, messageText, messageId);
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
            case ANOTHER_MONTH -> anotherMonth(session, locale, messageText, messageId);
        };
    }

    public void setExecutor(Consumer<BotApiMethod<?>> executor) {
        this.executor = executor;
    }


    private SendMessage selectMonth(ChatSession session, Locale locale) {
        String text = messageSource.getMessage(TextKeys.SELECT_MONTH_TO_COUNT_PROFIT.name(), null, locale);
        InlineKeyboardMarkup keyboardMarkup = monthsAsKeyboard(locale);
        session.setCommand(BotCommands.COUNT_PROFIT);
        session.setStep(Steps.INPUT_MONTH.ordinal());
        chatSessionService.save(session);
        return createSendMessage(session.getChatId(), text, keyboardMarkup);
    }

    private BotApiMethod<?> inputMonth(ChatSession session, Locale locale, String messageText, int messageId) {
        if (executor == null) {
            throw new ConfigurationCustomException("Executor for %s is null".formatted(this.getClass().getSimpleName()));
        }
        String[] callbackData = ChatBotUtility.callBackQueryParse(messageText);
        Month month;
        try {
            month = Month.valueOf(callbackData[2]);
            YearMonth yearMonth = YearMonth.now().withMonth(month.getValue());
            ProfitRecord profitRecord = reportService.countProfitForMonth(yearMonth.getYear(), yearMonth.getMonthValue(), session.getUserId());
            return profitRecordAsMessage(session, locale, profitRecord, messageId);
        } catch (IllegalArgumentException ignored) {
            ButtonKeys buttonKeys = ButtonKeys.valueOf(callbackData[2]);
            if (buttonKeys == ButtonKeys.ANOTHER_MONTH) {
                String text = messageSource.getMessage(TextKeys.INPUT_ANOTHER_MONTH_FOR_REPORT.name(), null, locale);
                session.setCommand(BotCommands.COUNT_PROFIT);
                session.setStep(Steps.ANOTHER_MONTH.ordinal());
                chatSessionService.save(session);
                return editMessageText(session.getChatId(), messageId, text);
            } else {
                throw new BadRequestCustomException("Unexpected response: " + callbackData[2]);
            }
        }
    }

    private SendMessage anotherMonth(ChatSession session, Locale locale, String messageText, int messageId) {
        if (executor == null) {
            throw new ConfigurationCustomException("Executor for %s is null".formatted(this.getClass().getSimpleName()));
        }
        String[] dateValue = messageText.strip().split("\\.");
        if (dateValue.length > 2) {
            throw new BadRequestCustomException("Incorrect value inputted: " + messageText);
        }
        if (dateValue.length == 1) {
            messageText += "." + LocalDate.now().getYear();
        }
        YearMonth yearMonth = YearMonth.parse(messageText, DateTimeFormatter.ofPattern("MM.yyyy"));
        ProfitRecord profitRecord = reportService.countProfitForMonth(yearMonth.getYear(), yearMonth.getMonthValue(), session.getUserId());
        return profitRecordAsMessage(session, locale, profitRecord, messageId);
    }

    private InlineKeyboardMarkup monthsAsKeyboard(Locale locale) {
        LocalDate now = LocalDate.now();
        Month current = now.getMonth();
        Month previous = now.minusMonths(1).getMonth();
        String callbackPrefix = ChatBotUtility.callBackQueryPrefix(BotCommands.COUNT_PROFIT, Steps.INPUT_MONTH.ordinal());
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

    private SendMessage profitRecordAsMessage(ChatSession session, Locale locale, ProfitRecord profitRecord, int messageId) {
        String month = profitRecord.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, locale).toUpperCase();
        String text = month + " - " + profitRecord.getYear() + '\n' + profitRecord.getValue();
        session.reset();
        chatSessionService.save(session);
        executor.accept(deleteMessage(session.getChatId(), messageId));
        return createSendMessage(session.getChatId(), text);
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

package org.lab.telegram_bot.domain.command.handlers;

import org.lab.model.DentalWork;
import org.lab.model.Product;
import org.lab.telegram_bot.domain.command.BotCommands;
import org.lab.telegram_bot.domain.command.CommandHandler;
import org.lab.telegram_bot.domain.command.TextKeys;
import org.lab.telegram_bot.domain.element.ButtonKeys;
import org.lab.telegram_bot.domain.element.KeyboardBuilderKit;
import org.lab.telegram_bot.domain.session.ChatSession;
import org.lab.telegram_bot.domain.session.ChatSessionService;
import org.lab.telegram_bot.service.DentalLabRestClientWrapper;
import org.lab.telegram_bot.service.DentalWorkServiceWrapper;
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
import java.util.List;
import java.util.Locale;

@CommandHandler(command = BotCommands.WORK_FOR_TOMORROW)
public class WorkForTomorrowCommandHandler extends BotCommandHandler {

    private final DentalWorkServiceWrapper dentalWorkService;
    private final ChatSessionService chatSessionService;
    private final KeyboardBuilderKit keyboardBuilderKit;


    @Autowired
    public WorkForTomorrowCommandHandler(MessageSource messageSource,
                                         DentalLabRestClientWrapper dentalLabRestClient,
                                         ChatSessionService chatSessionService,
                                         KeyboardBuilderKit keyboardBuilderKit) {
        super(messageSource);
        this.dentalWorkService = dentalLabRestClient.DENTAL_WORKS;
        this.chatSessionService = chatSessionService;
        this.keyboardBuilderKit = keyboardBuilderKit;
    }


    @Override
    public BotApiMethod<?> handle(Message message, ChatSession session) {
        Locale locale = ChatBotUtility.getLocale(message);
        return getWorkList(session, locale);
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery, ChatSession session, Locale locale) {
        return getWorkList(session, locale);
    }


    private SendMessage getWorkList(ChatSession session, Locale locale) {
        List<DentalWork> dentalWorks = dentalWorkService.findAll(session.getUserId());
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        dentalWorks = dentalWorks.stream()
                .filter(dw -> dw.getCompleteAt().equals(tomorrow))
                .toList();
        dentalWorks.forEach(dw -> dw.getProducts().removeIf(p -> !p.getCompleteAt().equals(tomorrow)));
        String text = workForTomorrowListToMessage(dentalWorks, locale);
        session.setCommand(BotCommands.DENTAL_WORKS);
        session.setStep(DentalWorksHandler.Steps.LIST_PAGING.ordinal());
        chatSessionService.save(session);
        if (dentalWorks.isEmpty()) {
            return createSendMessage(session.getChatId(), text);
        } else {
            InlineKeyboardButton selectButton = buildSelectItemButton(locale);
            InlineKeyboardMarkup keyboardMarkup = keyboardBuilderKit.inlineKeyboard(List.of(selectButton));
            return createSendMessage(session.getChatId(), text, keyboardMarkup);
        }
    }

    private String workForTomorrowListToMessage(List<DentalWork> dentalWorks, Locale locale) {
        if (dentalWorks == null || dentalWorks.isEmpty()) {
            return messageSource.getMessage(TextKeys.EMPTY.name(), null, locale);
        }
        String template = messageSource.getMessage("WORK_FOR_TOMORROW_LIST_TEMPLATE", null, locale);
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
            if (!productStringBuilder.isEmpty()) {
                productStringBuilder.deleteCharAt(productStringBuilder.length() - 1);
            }
            String item = template.formatted(
                    dw.getId(),
                    dw.getPatient(),
                    dw.getClinic(),
                    productStringBuilder.toString());
            workStringBuilder.append(item)
                    .append('\n')
                    .append(ITEM_DELIMITER)
                    .append('\n');
            productStringBuilder.setLength(0);
        }
        return workStringBuilder.toString();
    }

    private InlineKeyboardButton buildSelectItemButton(Locale locale) {
        String callbackQueryPrefix = ChatBotUtility.callBackQueryPrefix(BotCommands.DENTAL_WORKS, DentalWorksHandler.Steps.SELECT_ITEM.ordinal());
        String callbackQueryData = callbackQueryPrefix + "null";
        String callbackLabel = messageSource.getMessage(ButtonKeys.SELECT_ITEM.name(), null, locale);
        return keyboardBuilderKit.callbackButton(callbackLabel, callbackQueryData);
    }
}

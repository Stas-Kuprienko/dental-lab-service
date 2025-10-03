package org.lab.telegram_bot.domain.command.handlers;

import org.lab.model.DentalWork;
import org.lab.telegram_bot.domain.command.BotCommands;
import org.lab.telegram_bot.domain.command.CommandHandler;
import org.lab.telegram_bot.domain.element.ButtonKeys;
import org.lab.telegram_bot.domain.element.KeyboardBuilderKit;
import org.lab.telegram_bot.domain.session.ChatSession;
import org.lab.telegram_bot.domain.session.ChatSessionService;
import org.lab.telegram_bot.service.DentalWorkMvcService;
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

    private final DentalWorkMvcService dentalWorkService;
    private final ChatSessionService chatSessionService;
    private final KeyboardBuilderKit keyboardBuilderKit;


    @Autowired
    public WorkForTomorrowCommandHandler(MessageSource messageSource,
                                         DentalWorkMvcService dentalWorkService,
                                         ChatSessionService chatSessionService,
                                         KeyboardBuilderKit keyboardBuilderKit) {
        super(messageSource);
        this.dentalWorkService = dentalWorkService;
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
        List<DentalWork> dentalWorks = dentalWorkService.getAll(session.getUserId());
        LocalDate tomorrow = LocalDate.now();
        dentalWorks = dentalWorks.stream()
                .filter(dw -> dw.getCompleteAt().equals(tomorrow))
                .toList();
        String text = workListToMessage(dentalWorks, locale);
        InlineKeyboardButton selectButton = buildSelectItemButton(locale);
        InlineKeyboardMarkup keyboardMarkup = keyboardBuilderKit.inlineKeyboard(List.of(selectButton));
        session.setCommand(BotCommands.DENTAL_WORKS);
        session.setStep(DentalWorksHandler.Steps.LIST_PAGING.ordinal());
        chatSessionService.save(session);
        return createSendMessage(session.getChatId(), text, keyboardMarkup);
    }

    private InlineKeyboardButton buildSelectItemButton(Locale locale) {
        String callbackQueryPrefix = ChatBotUtility.callBackQueryPrefix(BotCommands.DENTAL_WORKS, DentalWorksHandler.Steps.SELECT_ITEM.ordinal());
        String callbackQueryData = callbackQueryPrefix + "null";
        String callbackLabel = messageSource.getMessage(ButtonKeys.SELECT_ITEM.name(), null, locale);
        return keyboardBuilderKit.callbackButton(callbackLabel, callbackQueryData);
    }
}

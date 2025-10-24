package org.lab.telegram_bot.domain.command.handlers;

import org.lab.exception.BadRequestCustomException;
import org.lab.model.DentalWork;
import org.lab.telegram_bot.domain.command.BotCommands;
import org.lab.telegram_bot.domain.command.CommandHandler;
import org.lab.telegram_bot.domain.command.TextKeys;
import org.lab.telegram_bot.domain.element.ButtonKeys;
import org.lab.telegram_bot.domain.element.KeyboardBuilderKit;
import org.lab.telegram_bot.domain.session.ChatSession;
import org.lab.telegram_bot.domain.session.ChatSessionService;
import org.lab.telegram_bot.exception.ApplicationCustomException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

@CommandHandler(command = BotCommands.DENTAL_WORKS)
public class DentalWorksHandler extends BotCommandHandler {

    private static final int PAGE_ITEMS = 10;

    private final DentalWorkMvcService dentalWorkService;
    private final KeyboardBuilderKit keyboardBuilderKit;
    private final ChatSessionService chatSessionService;
    private Consumer<BotApiMethod<?>> executor;


    @Autowired
    public DentalWorksHandler(DentalWorkMvcService dentalWorkService,
                              KeyboardBuilderKit keyboardBuilderKit,
                              MessageSource messageSource,
                              ChatSessionService chatSessionService) {
        super(messageSource);
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
            case GET_WORK_LIST -> getList(session, locale, messageId);
            case LIST_PAGING -> paging(session, locale, messageText, messageId);
            case SELECT_ITEM -> selectItem(session, locale, messageText, messageId);
            case INPUT_WORK_ID -> inputWorkId(session, locale, messageText, messageId);
        };
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery, ChatSession session, Locale locale) {
        String messageText = callbackQuery.getData();
        int messageId = callbackQuery.getMessage().getMessageId();
        Steps step = getStep(session);
        return switch (step) {
            case GET_WORK_LIST -> getList(session, locale, messageId);
            case LIST_PAGING -> paging(session, locale, messageText, messageId);
            case SELECT_ITEM -> selectItem(session, locale, messageText, messageId);
            case INPUT_WORK_ID -> inputWorkId(session, locale, messageText, messageId);
        };
    }

    public void setExecutor(Consumer<BotApiMethod<?>> executor) {
        this.executor = executor;
    }


    private SendMessage getList(ChatSession session, Locale locale, int messageId) {
        List<DentalWork> dentalWorks = dentalWorkService.getAll(session.getUserId());
        ListPage listPage = getSubListForPage(dentalWorks, 1);
        String text = workListToMessage(listPage.dentalWorks, locale);
        List<List<InlineKeyboardButton>> buttonLists = new ArrayList<>();
        if (!listPage.isLast) {
            buttonLists.add(List.of(buildNextButton(locale, 2)));
        }
        if (!dentalWorks.isEmpty()) {
            buttonLists.add(List.of(buildSelectItemButton(locale, messageId)));
        }
        session.setCommand(BotCommands.DENTAL_WORKS);
        session.setStep(Steps.LIST_PAGING.ordinal());
        chatSessionService.save(session);
        if (buttonLists.isEmpty()) {
            return createSendMessage(session.getChatId(), text);
        } else {
            InlineKeyboardMarkup keyboardMarkup = keyboardBuilderKit.inlineKeyboard(buttonLists);
            return createSendMessage(session.getChatId(), text, keyboardMarkup);
        }
    }

    private BotApiMethod<?> paging(ChatSession session, Locale locale, String messageText, int messageId) {
        String[] callbackData = ChatBotUtility.callBackQueryParse(messageText);
        if (callbackData[2].equals(ButtonKeys.CANCEL.name())) {
            session.reset();
            chatSessionService.save(session);
            return deleteMessage(session.getChatId(), messageId);
        }
        int page = Integer.parseInt(callbackData[2]);
        List<DentalWork> dentalWorks = dentalWorkService.getAll(session.getUserId());
        ListPage listPage = getSubListForPage(dentalWorks, page);
        String text = workListToMessage(listPage.dentalWorks, locale);
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        if (!listPage.isLast) {
            buttons.add(buildNextButton(locale, page + 1));
        }
        if (page > 1) {
            buttons.add(buildPreviousButton(locale, page - 1));
        }
        List<List<InlineKeyboardButton>> buttonLists = new ArrayList<>();
        buttonLists.add(buttons);
        if (!dentalWorks.isEmpty()) {
            buttonLists.add(List.of(buildSelectItemButton(locale, messageId)));
        }
        InlineKeyboardMarkup keyboardMarkup = keyboardBuilderKit.inlineKeyboard(buttonLists);
        session.setCommand(BotCommands.DENTAL_WORKS);
        session.setStep(Steps.LIST_PAGING.ordinal());
        chatSessionService.save(session);
        return editMessageText(session.getChatId(), messageId, text, keyboardMarkup);
    }

    private SendMessage selectItem(ChatSession session, Locale locale, String messageText, int messageId) {
//        String[] callbackData = ChatBotUtility.callBackQueryParse(messageText);
        String text = messageSource.getMessage(TextKeys.INPUT_WORK_ID_TO_OPEN.name(), null, locale);
        String callbackQueryPrefix = ChatBotUtility.callBackQueryPrefix(BotCommands.DENTAL_WORKS, Steps.INPUT_WORK_ID.ordinal());
        InlineKeyboardButton cancelButton = keyboardBuilderKit.callbackButton(ButtonKeys.CANCEL, callbackQueryPrefix, locale);
        session.addAttribute(Attributes.MESSAGE_ID_TO_DELETE.name(), Integer.toString(messageId));
        session.setCommand(BotCommands.DENTAL_WORKS);
        session.setStep(Steps.INPUT_WORK_ID.ordinal());
        chatSessionService.save(session);
        return createSendMessage(session.getChatId(), text, keyboardBuilderKit.inlineKeyboard(List.of(cancelButton)));
    }

    private BotApiMethod<?> inputWorkId(ChatSession session, Locale locale, String messageText, int messageId) {
        if (executor == null) {
            throw new ApplicationCustomException("Executor for %s is null".formatted(this.getClass().getSimpleName()));
        }
        if (isCancel(session, messageText, messageId)) {
            return getList(session, locale, messageId);
        }
        int messageToDelete = Integer.parseInt(session.getAttribute(Attributes.MESSAGE_ID_TO_DELETE.name()));
        long workId = Long.parseLong(messageText);
        DentalWork dentalWork = dentalWorkService.getById(workId, session.getUserId());
        return viewDentalWork(
                keyboardBuilderKit,
                chatSessionService,
                session,
                locale,
                dentalWork,
                executor,
                messageToDelete, messageId, messageId - 1);
    }

    private boolean isCancel(ChatSession session, String messageText, int messageId) {
        try {
            String[] callbackData = ChatBotUtility.callBackQueryParse(messageText);
            if (callbackData[2].equals(ButtonKeys.CANCEL.name())) {
                executor.accept(deleteMessage(session.getChatId(), messageId));
                return true;
            }
        } catch (IllegalArgumentException ignored) {}
        return false;
    }

    private ListPage getSubListForPage(List<DentalWork> dentalWorks, int page) {
        int lastIndex = page * PAGE_ITEMS;
        int firstIndex = lastIndex - PAGE_ITEMS;
        int size = dentalWorks.size();
        if (firstIndex > size) {
            throw new BadRequestCustomException("Going beyond the list of entries");
        } else if (lastIndex > size) {
            return new ListPage(dentalWorks.subList(firstIndex, size), true);
        } else {
            return new ListPage(dentalWorks.subList(firstIndex, lastIndex), dentalWorks.size() <= PAGE_ITEMS);
        }
    }

    private InlineKeyboardButton buildNextButton(Locale locale, int nextPageNumber) {
        String callbackQueryPrefix = ChatBotUtility.callBackQueryPrefix(BotCommands.DENTAL_WORKS, Steps.LIST_PAGING.ordinal());
        String callbackQueryData = callbackQueryPrefix + nextPageNumber;
        String callbackLabel = messageSource.getMessage(ButtonKeys.NEXT.name(), null, locale);
        return keyboardBuilderKit.callbackButton(callbackLabel, callbackQueryData);
    }

    private InlineKeyboardButton buildPreviousButton(Locale locale, int previousPageNumber) {
        String callbackQueryPrefix = ChatBotUtility.callBackQueryPrefix(BotCommands.DENTAL_WORKS, Steps.LIST_PAGING.ordinal());
        String callbackQueryData = callbackQueryPrefix + previousPageNumber;
        String callbackLabel = messageSource.getMessage(ButtonKeys.BACK.name(), null, locale);
        return keyboardBuilderKit.callbackButton(callbackLabel, callbackQueryData);
    }

    private InlineKeyboardButton buildSelectItemButton(Locale locale, int messageId) {
        String callbackQueryPrefix = ChatBotUtility.callBackQueryPrefix(BotCommands.DENTAL_WORKS, Steps.SELECT_ITEM.ordinal());
        String callbackQueryData = callbackQueryPrefix + messageId;
        String callbackLabel = messageSource.getMessage(ButtonKeys.SELECT_ITEM.name(), null, locale);
        return keyboardBuilderKit.callbackButton(callbackLabel, callbackQueryData);
    }

    private Steps getStep(ChatSession session) {
        return Steps.values()[session.getStep()];
    }


    enum Steps {
        GET_WORK_LIST,
        LIST_PAGING,
        SELECT_ITEM,
        INPUT_WORK_ID
    }

    enum Attributes {
        MESSAGE_ID_TO_DELETE
    }

    private record ListPage(List<DentalWork> dentalWorks, boolean isLast) {}
}

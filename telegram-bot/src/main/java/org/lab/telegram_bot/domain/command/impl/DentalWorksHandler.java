package org.lab.telegram_bot.domain.command.impl;

import org.lab.exception.BadRequestCustomException;
import org.lab.model.DentalWork;
import org.lab.model.Product;
import org.lab.telegram_bot.domain.command.BotCommandHandler;
import org.lab.telegram_bot.domain.command.BotCommands;
import org.lab.telegram_bot.domain.command.CommandHandler;
import org.lab.telegram_bot.domain.element.ButtonKeys;
import org.lab.telegram_bot.domain.element.KeyboardBuilderKit;
import org.lab.telegram_bot.domain.session.ChatSession;
import org.lab.telegram_bot.domain.session.ChatSessionService;
import org.lab.telegram_bot.exception.ConfigurationCustomException;
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

    private static final String WORK_LIST_TEMPLATE = "WORK_LIST_TEMPLATE";
    private static final String ITEM_DELIMITER = "\n******************\n";
    private static final int PAGE_ITEMS = 10;

    private final DentalWorkMvcService dentalWorkMvcService;
    private final KeyboardBuilderKit keyboardBuilderKit;
    private final ChatSessionService chatSessionService;
    private Consumer<BotApiMethod<?>> executor;


    @Autowired
    public DentalWorksHandler(DentalWorkMvcService dentalWorkMvcService,
                              KeyboardBuilderKit keyboardBuilderKit,
                              MessageSource messageSource,
                              ChatSessionService chatSessionService) {
        super(messageSource);
        this.dentalWorkMvcService = dentalWorkMvcService;
        this.keyboardBuilderKit = keyboardBuilderKit;
        this.chatSessionService = chatSessionService;
    }


    @Override
    public BotApiMethod<?> handle(Message message, ChatSession session) {
        Locale locale = ChatBotUtility.getLocale(message);
        String messageText = message.getText();
        Steps steps = getStep(session);
        return switch (steps) {
            case GET_WORK_LIST -> getList(session, locale, message.getMessageId());
            case WORK_LIST_PAGING -> paging(session, locale, messageText, message.getMessageId());
            case SELECT_ITEM_FROM_WORK_LIST -> selectItem(session, locale, messageText, message.getMessageId());
            case INPUT_WORK_ID_FOR_SELECT -> inputWorkId(session, locale, messageText, message.getMessageId());
        };
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery, ChatSession session, Locale locale) {
        String messageText = callbackQuery.getData();
        Steps steps = getStep(session);
        return switch (steps) {
            case GET_WORK_LIST -> getList(session, locale, callbackQuery.getMessage().getMessageId());
            case WORK_LIST_PAGING -> paging(session, locale, messageText, callbackQuery.getMessage().getMessageId());
            case SELECT_ITEM_FROM_WORK_LIST -> selectItem(session, locale, messageText, callbackQuery.getMessage().getMessageId());
            case INPUT_WORK_ID_FOR_SELECT -> inputWorkId(session, locale, messageText, callbackQuery.getMessage().getMessageId());
        };
    }

    public void setExecutor(Consumer<BotApiMethod<?>> executor) {
        this.executor = executor;
    }


    private SendMessage getList(ChatSession session, Locale locale, int messageId) {
        List<DentalWork> dentalWorks = dentalWorkMvcService.getAll(session.getUserId());
        ListPage listPage = getSubListForPage(dentalWorks, 1);
        String text = workListToMessage(listPage.dentalWorks, locale);
        InlineKeyboardButton nextButton = buildNextButton(locale, 2);
        InlineKeyboardButton selectButton = buildSelectItemButton(locale, messageId);
        InlineKeyboardMarkup keyboardMarkup = keyboardBuilderKit.inlineKeyboard(List.of(nextButton), List.of(selectButton));
        session.setCommand(BotCommands.DENTAL_WORKS);
        session.setStep(Steps.WORK_LIST_PAGING.ordinal());
        chatSessionService.save(session);
        return createSendMessage(session.getChatId(), text, keyboardMarkup);
    }

    private BotApiMethod<?> paging(ChatSession session, Locale locale, String messageText, int messageId) {
        String[] callbackData = ChatBotUtility.callBackQueryParse(messageText);
        if (callbackData[2].equals(ButtonKeys.CANCEL.name())) {
            session.clear();
            chatSessionService.save(session);
            return deleteMessage(session.getChatId(), messageId);
        }
        int page = Integer.parseInt(callbackData[2]);
        List<DentalWork> dentalWorks = dentalWorkMvcService.getAll(session.getUserId());
        ListPage listPage = getSubListForPage(dentalWorks, page);
        String text = workListToMessage(listPage.dentalWorks, locale);
        InlineKeyboardButton selectButton = buildSelectItemButton(locale, messageId);
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        if (!listPage.isLast) {
            buttons.add(buildNextButton(locale, page + 1));
        }
        if (page > 1) {
            buttons.add(buildPreviousButton(locale, page - 1));
        }
        InlineKeyboardMarkup keyboardMarkup = keyboardBuilderKit.inlineKeyboard(buttons, List.of(selectButton));
        session.setCommand(BotCommands.DENTAL_WORKS);
        session.setStep(Steps.WORK_LIST_PAGING.ordinal());
        chatSessionService.save(session);
        return editMessageText(session.getChatId(), messageId, text, keyboardMarkup);
    }

    private SendMessage selectItem(ChatSession session, Locale locale, String messageText, int messageId) {
        String[] callbackData = ChatBotUtility.callBackQueryParse(messageText);
        String text = messageSource.getMessage(Steps.SELECT_ITEM_FROM_WORK_LIST.name(), null, locale);
        String callbackQueryPrefix = ChatBotUtility.callBackQueryPrefix(BotCommands.DENTAL_WORKS, Steps.INPUT_WORK_ID_FOR_SELECT.ordinal());
        InlineKeyboardButton cancelButton = keyboardBuilderKit.callbackButton(ButtonKeys.CANCEL, callbackQueryPrefix, locale);
        session.addAttribute(Attributes.MESSAGE_ID_TO_DELETE.name(), Integer.toString(messageId));
        session.setCommand(BotCommands.DENTAL_WORKS);
        session.setStep(Steps.INPUT_WORK_ID_FOR_SELECT.ordinal());
        chatSessionService.save(session);
        return createSendMessage(session.getChatId(), text, keyboardBuilderKit.inlineKeyboard(List.of(cancelButton)));
    }

    private BotApiMethod<?> inputWorkId(ChatSession session, Locale locale, String messageText, int messageId) {
        if (executor == null) {
            throw new ConfigurationCustomException("Executor for %s is null".formatted(this.getClass().getSimpleName()));
        }
        try {
            String[] callbackData = ChatBotUtility.callBackQueryParse(messageText);
            if (callbackData[2].equals(ButtonKeys.CANCEL.name())) {
                executor.accept(deleteMessage(session.getChatId(), messageId));
                return getList(session, locale, messageId);
            }
        } catch (IllegalArgumentException ignored) {}
        int messageToDelete = Integer.parseInt(session.getAttribute(Attributes.MESSAGE_ID_TO_DELETE.name()));
        long workId = Long.parseLong(messageText);
        DentalWork dentalWork = dentalWorkMvcService.getById(workId, session.getUserId());
        String text = dentalWorkAsMessage(dentalWork, locale);
        String buttonLabel = messageSource.getMessage(NewDentalWorkHandler.Steps.ADD_PRODUCT_TO_DENTAL_WORK.name(), null, locale);
        String callbackQueryData = ChatBotUtility.callBackQuery(BotCommands.NEW_DENTAL_WORK, NewDentalWorkHandler.Steps.ADD_PRODUCT_TO_DENTAL_WORK.ordinal(), dentalWork.getId().toString());
        InlineKeyboardButton addProductButton = keyboardBuilderKit.callbackButton(buttonLabel, callbackQueryData);
        InlineKeyboardMarkup inlineKeyboardMarkup = keyboardBuilderKit.inlineKeyboard(List.of(addProductButton));
        session.removeAttribute(NewDentalWorkHandler.Attributes.NEW_DENTAL_WORK.name());
        session.setCommand(BotCommands.NEW_DENTAL_WORK);
        session.setStep(NewDentalWorkHandler.Steps.ADD_PRODUCT_TO_DENTAL_WORK.ordinal());
        chatSessionService.save(session);
        executor.accept(deleteMessage(session.getChatId(), messageToDelete));
        executor.accept(deleteMessage(session.getChatId(), messageId));
        executor.accept(deleteMessage(session.getChatId(), messageId - 1));
        return createSendMessage(session.getChatId(), text, inlineKeyboardMarkup);
    }

    private String workListToMessage(List<DentalWork> dentalWorks, Locale locale) {
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

    private ListPage getSubListForPage(List<DentalWork> dentalWorks, int page) {
        int lastIndex = page * PAGE_ITEMS;
        int firstIndex = lastIndex - PAGE_ITEMS;
        int size = dentalWorks.size();
        if (firstIndex > size) {
            throw new BadRequestCustomException("Going beyond the list of entries");
        } else if (lastIndex > size) {
            return new ListPage(dentalWorks.subList(firstIndex, size), true);
        } else {
            return new ListPage(dentalWorks.subList(firstIndex, lastIndex), false);
        }
    }

    private InlineKeyboardButton buildNextButton(Locale locale, int nextPageNumber) {
        String callbackQueryPrefix = ChatBotUtility.callBackQueryPrefix(BotCommands.DENTAL_WORKS, Steps.WORK_LIST_PAGING.ordinal());
        String callbackQueryData = callbackQueryPrefix + nextPageNumber;
        String callbackLabel = messageSource.getMessage(ButtonKeys.NEXT.name(), null, locale);
        return keyboardBuilderKit.callbackButton(callbackLabel, callbackQueryData);
    }

    private InlineKeyboardButton buildPreviousButton(Locale locale, int previousPageNumber) {
        String callbackQueryPrefix = ChatBotUtility.callBackQueryPrefix(BotCommands.DENTAL_WORKS, Steps.WORK_LIST_PAGING.ordinal());
        String callbackQueryData = callbackQueryPrefix + previousPageNumber;
        String callbackLabel = messageSource.getMessage(ButtonKeys.BACK.name(), null, locale);
        return keyboardBuilderKit.callbackButton(callbackLabel, callbackQueryData);
    }

    private InlineKeyboardButton buildSelectItemButton(Locale locale, int messageId) {
        String callbackQueryPrefix = ChatBotUtility.callBackQueryPrefix(BotCommands.DENTAL_WORKS, Steps.SELECT_ITEM_FROM_WORK_LIST.ordinal());
        String callbackQueryData = callbackQueryPrefix + messageId;
        String callbackLabel = messageSource.getMessage(ButtonKeys.SELECT_ITEM.name(), null, locale);
        return keyboardBuilderKit.callbackButton(callbackLabel, callbackQueryData);
    }

    private Steps getStep(ChatSession session) {
        return Steps.values()[session.getStep()];
    }


    enum Steps {
        GET_WORK_LIST,
        WORK_LIST_PAGING,
        SELECT_ITEM_FROM_WORK_LIST,
        INPUT_WORK_ID_FOR_SELECT
    }

    enum Attributes {
        MESSAGE_ID_TO_DELETE
    }

    private record ListPage(List<DentalWork> dentalWorks, boolean isLast) {}
}

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
import org.lab.telegram_bot.exception.ConfigurationCustomException;
import org.lab.telegram_bot.service.DentalLabRestClientWrapper;
import org.lab.telegram_bot.service.DentalWorkMvcService;
import org.lab.telegram_bot.service.WorkPhotoLinkServiceWrapper;
import org.lab.telegram_bot.utils.ChatBotUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

@CommandHandler(command = BotCommands.PHOTO_FILES)
public class PhotoFilesCommandHandler extends BotCommandHandler {

    private final WorkPhotoLinkServiceWrapper workPhotoLinkService;
    private final DentalWorkMvcService dentalWorkService;
    private final ChatSessionService chatSessionService;
    private final KeyboardBuilderKit keyboardBuilderKit;
    private final String telegramApiPath;
    private final String botToken;
    private Function<GetFile, File> executor;
    private Consumer<SendPhoto> photoSender;


    @Autowired
    public PhotoFilesCommandHandler(MessageSource messageSource,
                                    DentalLabRestClientWrapper dentalLabRestClientWrapper,
                                    DentalWorkMvcService dentalWorkService,
                                    ChatSessionService chatSessionService,
                                    KeyboardBuilderKit keyboardBuilderKit,
                                    @Value("${telegram.api.path}") String telegramApiPath,
                                    @Value("${project.variables.telegram.botToken}") String botToken) {
        super(messageSource);
        this.dentalWorkService = dentalWorkService;
        this.workPhotoLinkService = dentalLabRestClientWrapper.PHOTO_LINKS;
        this.chatSessionService = chatSessionService;
        this.keyboardBuilderKit = keyboardBuilderKit;
        this.telegramApiPath = telegramApiPath;
        this.botToken = botToken;
    }


    @Override
    public BotApiMethod<?> handle(Message message, ChatSession session) {
        Locale locale = ChatBotUtility.getLocale(message);
        String messageText = message.getText();
        int messageId = message.getMessageId();
        Steps step = getStep(session);
        return switch (step) {
            case START_UPLOADING -> startUploading(session, locale, messageText, messageId);
            case WAITING_UPLOADING -> savePhoto(message, session, locale);
            case OPEN_PHOTOS -> openPhoto(session, locale, messageText, messageId);
        };
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery, ChatSession session, Locale locale) {
        String messageText = callbackQuery.getData();
        int messageId = callbackQuery.getMessage().getMessageId();
        Steps step = getStep(session);
        return switch (step) {
            case START_UPLOADING -> startUploading(session, locale, messageText, messageId);
            case WAITING_UPLOADING -> throw new BadRequestCustomException("Unexpected callback query: " + callbackQuery);
            case OPEN_PHOTOS -> openPhoto(session, locale, messageText, messageId);
        };
    }

    public void setExecutor(Function<GetFile, File> executor) {
        this.executor = executor;
    }

    public void setPhotoSender(Consumer<SendPhoto> photoSender) {
        this.photoSender = photoSender;
    }


    private EditMessageText startUploading(ChatSession session, Locale locale, String messageText, int messageId) {
        String[] callbackData = ChatBotUtility.callBackQueryParse(messageText);
        long workId = Long.parseLong(callbackData[2]);
        DentalWork dw = dentalWorkService.getById(workId, session.getUserId());
        session.addAttribute(Attributes.DENTAL_WORK_ID.name(), Long.toString(workId));
        String text = messageSource.getMessage(TextKeys.UPLOAD_PHOTO_TO_CHAT.name(), new Object[]{dw.getClinic(), dw.getPatient()}, locale);
        InlineKeyboardMarkup keyboardMarkup = keyboardBuilderKit.inlineKeyboard(cancelButton(locale));
        session.setCommand(BotCommands.PHOTO_FILES);
        session.setStep(Steps.WAITING_UPLOADING.ordinal());
        chatSessionService.save(session);
        return editMessageText(session.getChatId(), messageId, text, keyboardMarkup);
    }

    private SendMessage savePhoto(Message message, ChatSession session, Locale locale) {
        if (executor == null) {
            throw new ConfigurationCustomException("Executor for %s is null".formatted(this.getClass().getSimpleName()));
        }
        if (!message.hasPhoto()) {
            throw new ConfigurationCustomException("Photo files does not contents");
        }
        String photoId = message.getPhoto().stream()
                .max(Comparator.comparing(PhotoSize::getFileSize))
                .orElseThrow(() -> new ConfigurationCustomException("Photo files does not contents"))
                .getFileId();
        GetFile getFileMethod = new GetFile();
        getFileMethod.setFileId(photoId);
        File file = executor.apply(getFileMethod);
        String filePath = file.getFilePath();
        String fileUrl = telegramApiPath + botToken + '/' + filePath;
        long workId = Long.parseLong(session.getAttribute(Attributes.DENTAL_WORK_ID.name()));
        URL url;
        try {
            url = URI.create(fileUrl).toURL();
            try (InputStream inputStream = url.openStream()) {
                byte[] bytes = inputStream.readAllBytes();
                workPhotoLinkService.create(workId, bytes, session.getUserId());
                String photoIsAddedText = messageSource.getMessage(TextKeys.PHOTO_IS_ADDED.name(), null, locale);
                String uploadPhotoText = messageSource.getMessage(TextKeys.UPLOAD_PHOTO_TO_CHAT.name(), null, locale);
                String text = photoIsAddedText + '\n' + uploadPhotoText;
                InlineKeyboardMarkup keyboardMarkup = keyboardBuilderKit.inlineKeyboard(cancelButton(locale));
                session.setCommand(BotCommands.PHOTO_FILES);
                session.setStep(Steps.WAITING_UPLOADING.ordinal());
                chatSessionService.save(session);
                return createSendMessage(session.getChatId(), text, keyboardMarkup);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SendMessage openPhoto(ChatSession session, Locale locale, String messageText, int messageId) {

//        SendPhoto sendPhoto = new SendPhoto();
//        sendPhoto.setChatId(session.getChatId());
//
//        photoSender.accept(sendPhoto);

        return createSendMessage(session.getChatId(), "НИХУЯ!");
    }

    private List<InlineKeyboardButton> cancelButton(Locale locale) {
        String callbackQueryPrefix = ChatBotUtility.callBackQueryPrefix(BotCommands.CLEAR, 0);
        return List.of(keyboardBuilderKit.callbackButton(ButtonKeys.CANCEL, callbackQueryPrefix, locale));
    }

    private Steps getStep(ChatSession session) {
        return Steps.values()[session.getStep()];
    }


    enum Steps {
        START_UPLOADING,
        WAITING_UPLOADING,
        OPEN_PHOTOS
    }

    enum Attributes {
        DENTAL_WORK_ID
    }
}

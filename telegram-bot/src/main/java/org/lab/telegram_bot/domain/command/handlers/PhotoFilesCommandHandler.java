package org.lab.telegram_bot.domain.command.handlers;

import org.lab.telegram_bot.domain.command.BotCommands;
import org.lab.telegram_bot.domain.command.CommandHandler;
import org.lab.telegram_bot.domain.session.ChatSession;
import org.lab.telegram_bot.domain.session.ChatSessionService;
import org.lab.telegram_bot.exception.ConfigurationCustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;

@CommandHandler(command = BotCommands.PHOTO_FILES)
public class PhotoFilesCommandHandler extends BotCommandHandler {

    private final ChatSessionService chatSessionService;
    private Function<GetFile, File> executor;


    @Autowired
    public PhotoFilesCommandHandler(MessageSource messageSource,
                                    ChatSessionService chatSessionService) {
        super(messageSource);
        this.chatSessionService = chatSessionService;
    }


    @Override
    public BotApiMethod<?> handle(Message message, ChatSession session) {

        return null;
    }

    @Override
    public BotApiMethod<?> handle(CallbackQuery callbackQuery, ChatSession session, Locale locale) {
        return null;
    }

    public void setExecutor(Function<GetFile, File> executor) {
        this.executor = executor;
    }


    private SendMessage savePhoto(Message message, ChatSession session) {
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

        // 3. Формируем URL для скачивания
        String fileUrl = "https://api.telegram.org/file/bot" + "getBotToken()" + "/" + filePath;

        long workId = Long.parseLong(session.getAttribute(Attributes.DENTAL_WORK_ID.name()));
        URL url;
        try {
            url = URI.create(fileUrl).toURL();
            try (InputStream inputStream = url.openStream()) {
                byte[] bytes = inputStream.readAllBytes();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    enum Attributes {
        DENTAL_WORK_ID
    }
}

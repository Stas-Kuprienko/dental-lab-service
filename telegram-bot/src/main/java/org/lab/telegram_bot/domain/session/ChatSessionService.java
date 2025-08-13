package org.lab.telegram_bot.domain.session;

import org.lab.model.TelegramChat;
import org.lab.telegram_bot.datasource.ChatSessionRepository;
import org.lab.telegram_bot.exception.UnregisteredUserException;
import org.lab.telegram_bot.service.DentalLabRestClientWrapper;
import org.lab.telegram_bot.service.TelegramChatServiceWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class ChatSessionService {

    private final ChatSessionRepository repository;
    private final TelegramChatServiceWrapper telegramChatService;

    @Autowired
    public ChatSessionService(ChatSessionRepository repository, DentalLabRestClientWrapper dentalLabRestClient) {
        this.repository = repository;
        this.telegramChatService = dentalLabRestClient.TELEGRAM_CHATS;
    }


    public ChatSession create(long chatId, UUID userId) {
        ChatSession session = ChatSession.createNew(chatId, userId);
        if (repository.save(session)) {
            return session;
        } else {
            //TODO
            throw new RuntimeException();
        }
    }

    public boolean save(ChatSession chatSession) {
        return repository.save(chatSession);
    }

    public ChatSession get(long chatId) {
        ChatSession session = repository.find(chatId);
        if (session == null) {
             TelegramChat chat = telegramChatService
                    .get(chatId)
                    .orElseThrow(() -> new UnregisteredUserException(chatId));
            session = create(chatId, chat.getUserId());
        }
        return session;
    }
}

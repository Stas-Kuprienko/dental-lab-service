package org.lab.telegram_bot.domain.session;

import org.dental.restclient.DentalLabRestClient;
import org.dental.restclient.TelegramChatService;
import org.lab.model.TelegramChat;
import org.lab.telegram_bot.datasource.ChatSessionRepository;
import org.lab.telegram_bot.exception.UnregisteredUserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class ChatSessionService {

    private final ChatSessionRepository repository;
    private final TelegramChatService telegramChatService;

    @Autowired
    public ChatSessionService(ChatSessionRepository repository, DentalLabRestClient dentalLabRestClient) {
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

package org.lab.telegram_bot.domain.session;

import feign.FeignException;
import org.lab.dental.feignclient.TelegramChatService;
import org.lab.model.TelegramChat;
import org.lab.telegram_bot.datasource.ChatSessionRepository;
import org.lab.telegram_bot.exception.ApplicationCustomException;
import org.lab.telegram_bot.exception.UnregisteredUserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ChatSessionService {

    private final ChatSessionRepository repository;
    private final TelegramChatService telegramChatService;

    @Autowired
    public ChatSessionService(ChatSessionRepository repository, TelegramChatService telegramChatService) {
        this.repository = repository;
        this.telegramChatService = telegramChatService;
    }


    public ChatSession create(long chatId, UUID userId) {
        ChatSession session = ChatSession.createNew(chatId, userId);
        if (repository.save(session)) {
            return session;
        } else {
            throw new ApplicationCustomException("Caching error");
        }
    }

    public boolean save(ChatSession chatSession) {
        return repository.save(chatSession);
    }

    public ChatSession get(long chatId) {
        ChatSession session = repository.find(chatId);
        if (session == null) {
             try {
                 TelegramChat chat = telegramChatService
                         .findByChatId(chatId);
                 session = create(chatId, chat.getUserId());
             } catch (FeignException.FeignClientException.NotFound e) {
                 throw new UnregisteredUserException(chatId);
             }
        }
        return session;
    }
}

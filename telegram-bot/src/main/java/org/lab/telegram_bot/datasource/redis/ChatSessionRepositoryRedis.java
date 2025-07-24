package org.lab.telegram_bot.datasource.redis;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.lab.telegram_bot.datasource.ChatSessionRepository;
import org.lab.telegram_bot.domain.session.ChatSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Slf4j
@Repository
public class ChatSessionRepositoryRedis implements ChatSessionRepository {

    private static final String CHAT_SESSION_KEY = "CHAT_SESSION";

    private final RedisTemplate<String, ChatSession> redisTemplate;


    @Autowired
    public ChatSessionRepositoryRedis(@Qualifier("chatSessionRedisTemplate") RedisTemplate<String, ChatSession> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    @PostConstruct
    public void init() {
        Duration duration = Duration.of(5, ChronoUnit.MINUTES);
        redisTemplate.expire(CHAT_SESSION_KEY, duration);
        log.info("Cache duration for {} key is set to {}", CHAT_SESSION_KEY, duration);
    }

    @Override
    public boolean save(ChatSession chatSession) {
        redisTemplate
                .opsForHash()
                .put(CHAT_SESSION_KEY, chatSession.getChatId().toString(), chatSession);
        log.debug("The chat session for ID {} is cached", chatSession.getChatId());
        return true;
    }

    @Override
    public ChatSession find(long chatId) {
        ChatSession session = (ChatSession) redisTemplate
                .opsForHash()
                .get(CHAT_SESSION_KEY, String.valueOf(chatId));
        log.debug("The chat session for ID {} is retrieved", chatId);
        return session;
    }
}

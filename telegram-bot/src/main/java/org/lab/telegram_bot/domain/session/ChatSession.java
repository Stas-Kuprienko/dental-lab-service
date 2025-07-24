package org.lab.telegram_bot.domain.session;

import lombok.*;
import org.lab.telegram_bot.domain.command.BotCommands;
import org.springframework.data.redis.core.RedisHash;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@RedisHash("chat_session")
public class ChatSession {

    private Long chatId;
    private UUID userId;
    private Context context;


    public ChatSession() {}


    public static ChatSession createNew(long chatId, UUID userId) {
        return new ChatSession(chatId, userId, new Context(null, new HashMap<>(), 0));
    }

    public Map<String, String> getAttributes() {
        return context.attributes;
    }

    public void clear() {
        context.setCommand(null);
        context.setStep(0);
        context.attributes.clear();
    }


    @Getter @Setter
    public static class Context {

        private BotCommands command;
        private Map<String, String> attributes;
        private int step;

        public Context(BotCommands command, Map<String, String> attributes, int step) {
            this.command = command;
            this.attributes = attributes;
            this.step = step;
        }

        public Context() {}
    }
}

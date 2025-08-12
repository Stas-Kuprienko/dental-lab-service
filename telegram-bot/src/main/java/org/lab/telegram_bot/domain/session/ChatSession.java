package org.lab.telegram_bot.domain.session;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.lab.telegram_bot.domain.command.BotCommands;
import org.springframework.data.redis.core.RedisHash;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    public Long getChatId() {
        return chatId;
    }

    public UUID getUserId() {
        return userId;
    }

    public Map<String, String> getAttributes() {
        return context.attributes;
    }

    public void setCommand(BotCommands command) {
        context.setCommand(command);
    }

    public BotCommands getCommand() {
        return context.getCommand();
    }

    public void setStep(int step) {
        context.setStep(step);
    }

    public int getStep() {
        return context.getStep();
    }

    public void clear() {
        context.setCommand(null);
        context.setStep(0);
        context.attributes.clear();
    }


    @Getter
    @Setter
    static class Context {

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

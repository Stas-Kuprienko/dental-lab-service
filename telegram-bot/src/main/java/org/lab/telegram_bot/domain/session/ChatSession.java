package org.lab.telegram_bot.domain.session;

import lombok.*;
import org.lab.telegram_bot.domain.command.BotCommands;
import org.springframework.data.redis.core.RedisHash;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
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

    public Object getAttribute(Object key) {
        return context.attributes.get(key);
    }

    public void addAttribute(Object key, Object value) {
        context.attributes.put(key, value);
    }

    public void removeAttribute(Object key) {
        context.attributes.remove(key);
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


    @Getter @Setter
    public static class Context {

        private BotCommands command;
        private Map<Object, Object> attributes;
        private int step;

        public Context(BotCommands command, Map<Object, Object> attributes, int step) {
            this.command = command;
            this.attributes = attributes;
            this.step = step;
        }

        public Context() {}
    }
}

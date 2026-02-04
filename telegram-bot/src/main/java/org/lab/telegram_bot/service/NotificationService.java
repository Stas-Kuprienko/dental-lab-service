package org.lab.telegram_bot.service;

import lombok.extern.slf4j.Slf4j;
import org.lab.event.EventMessage;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import java.util.function.Consumer;

@Slf4j
@Service
public class NotificationService {

    private Consumer<BotApiMethod<?>> executor;


    public void setExecutor(Consumer<BotApiMethod<?>> executor) {
        this.executor = executor;
    }

    public void sendMessageToChat(EventMessage message) {
        executor.accept(buildSendMessage(message));
        log.info("Event {} is sent to ChatId={}", message.getId(), message.getChatId());
    }


    private SendMessage buildSendMessage(EventMessage message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText(message.getText());
        return sendMessage;
    }
}

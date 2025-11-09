package org.lab.telegram_bot.service;

import lombok.extern.slf4j.Slf4j;
import org.lab.event.EventMessage;
import org.lab.telegram_bot.configuration.TelegramBotConfig;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import java.util.function.Consumer;

@Slf4j
@Service
@KafkaListener(topics = "event-topic")
public class NotificationService {

    private Consumer<BotApiMethod<?>> executor;



    public void setExecutor(Consumer<BotApiMethod<?>> executor) {
        this.executor = executor;
    }

    @KafkaHandler
    public void sendMessageToChat(EventMessage message) {
        log.info("Received event {} by Apache Kafka for ChatId={}. Event created at '{}'", message.getId(), message.getChatId(), message.getCreatedAt().format(TelegramBotConfig.DATE_TIME_FORMAT));
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

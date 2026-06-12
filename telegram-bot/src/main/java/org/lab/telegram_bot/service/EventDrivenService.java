package org.lab.telegram_bot.service;

import lombok.extern.slf4j.Slf4j;
import org.lab.event.EventMessage;
import org.lab.exception.ApplicationCustomException;
import org.lab.telegram_bot.exception.TelegramApiExceptionWrapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import java.net.ConnectException;

@Slf4j
@Service
public class EventDrivenService {

    private final NotificationService notificationService;

    @Autowired
    public EventDrivenService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }


    @RabbitListener(queues = "${spring.rabbitmq.queue}")
    @Retryable(retryFor = {ConnectException.class, TelegramApiExceptionWrapper.class, TelegramApiException.class},
            maxAttempts = 2, backoff = @Backoff(delay = 2000L))
    public void handle(EventMessage message) {
        log.info("Accept EventMessage with ID='{}', type={}", message.getId(), message.getType());
        try {
            notificationService.sendMessageToChat(message);
        } catch (TelegramApiExceptionWrapper e) {
            log.error(e.getMessage(), e);
            if (e.getCause().getClass().equals(TelegramApiRequestException.class)) {
                TelegramApiRequestException requestException = (TelegramApiRequestException) e.getCause();
                int code = requestException.getErrorCode();
                switch (code) {
                    case 408, 418, 429, 500, 503, 504 -> throw e;
                }
            }
            throw new ApplicationCustomException(e);
        }
    }
}

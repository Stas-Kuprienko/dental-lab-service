package org.lab.dental.service;

import lombok.extern.slf4j.Slf4j;
import org.lab.dental.util.metrics.ServiceMetrics;
import org.lab.event.EventMessage;
import org.lab.event.EventType;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Slf4j
@Service
public class EventFallbackService {

    private final ServiceMetrics metrics;
    private final UserService userService;
    private final NotificationService notificationService;


    @Autowired
    public EventFallbackService(ServiceMetrics metrics, UserService userService, NotificationService notificationService) {
        this.metrics = metrics;
        this.userService = userService;
        this.notificationService = notificationService;
    }


    @RabbitListener(queues = "${spring.rabbitmq.dlq}")
    @Retryable
    public void handle(EventMessage message) {
        EventType type = message.getType();
        log.info("Accept EventMessage with ID='{}', type={}", message.getId(), type);
        switch (type) {
            case OTP -> otpFallback(message);
            case MAILING -> mailingFallback(message);
            default -> log.error("Failed event message with ID='{}', with unexpected type='{}'", message.getId(), type);
        }
    }


    private void otpFallback(EventMessage message) {
        metrics.getOtpEventFails().increment();
        log.error("Failed OTP sending to Telegram, message ID='{}', chat ID='{}'", message.getId(), message.getChatId());
    }

    private void mailingFallback(EventMessage message) {
        metrics.getTelegramMailingFails().increment();
        log.warn("Failed Telegram mailing sending, message ID='{}', chat ID='{}'", message.getId(), message.getChatId());
        UUID userId = message.getUserId();
        String email = userService.getEmail(userId);
        notificationService.sendMailingEventToEmail(userId, email, message.getText());
    }
}

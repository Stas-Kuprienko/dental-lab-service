package org.lab.telegram_bot.service;

import lombok.extern.slf4j.Slf4j;
import org.lab.event.EventMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EventDrivenService {

    private final NotificationService notificationService;

    @Autowired
    public EventDrivenService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }


    @RabbitListener(queues = "${spring.rabbitmq.queue}")
    public void handle(EventMessage message) {
        log.info("Accept EventMessage with ID='{}'", message.getId());
        notificationService.sendMessageToChat(message);
    }
}

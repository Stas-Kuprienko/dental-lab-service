package org.lab.dental.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.lab.dental.service.EventMessageService;
import org.lab.event.EventMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RabbitMqEventMessageService implements EventMessageService {

    private final RabbitTemplate rabbitTemplate;
    private final String topicName;
    private final String routingKey;

    @Autowired
    public RabbitMqEventMessageService(RabbitTemplate rabbitTemplate,
                                       @Value("${spring.rabbitmq.exchange}") String topicName,
                                       @Value("${spring.rabbitmq.routing-key}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.topicName = topicName;
        this.routingKey = routingKey;
    }


    @Override
    public void send(EventMessage message) {
        log.info("Accept EventMessage with ID='{}', created at '{}'", message.getId(), message.getCreatedAt());
        rabbitTemplate.convertAndSend(topicName, routingKey, message,
                m -> {log.info("Send message (ID='{}', EventMessage ID='{}') to RabbitMq, exchange='{}'",
                        m.getMessageProperties().getMessageId(), message.getId(), m.getMessageProperties().getReceivedExchange());
            return m;
        });
    }
}

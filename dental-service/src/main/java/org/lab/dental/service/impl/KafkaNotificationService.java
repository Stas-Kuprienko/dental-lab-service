package org.lab.dental.service.impl;

import org.lab.event.EventMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

@Service
public class KafkaNotificationService {

    private final String topic;
    private final KafkaTemplate<String, EventMessage> kafkaTemplate;

    @Autowired
    public KafkaNotificationService(@Value("${spring.kafka.topic}") String topic,
                                    KafkaTemplate<String, EventMessage> kafkaTemplate) {
        this.topic = topic;
        this.kafkaTemplate = kafkaTemplate;
    }


    public CompletableFuture<SendResult<String, EventMessage>> send(EventMessage message) {
        return kafkaTemplate.send(topic, message.getId().toString(), message);
    }
}

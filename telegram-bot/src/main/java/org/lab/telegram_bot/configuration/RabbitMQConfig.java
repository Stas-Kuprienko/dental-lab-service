package org.lab.telegram_bot.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    private final String EXCHANGE;
    private final String DLX;
    private final String EVENT_QUEUE;
    private final String DLQ;
    private final String routingKey;
    private final String dlqRoutingKey;


    @Autowired
    public RabbitMQConfig(@Value("${spring.rabbitmq.exchange}") String exchange,
                          @Value("${spring.rabbitmq.dlq-exchange}") String dlqExchange,
                          @Value("${spring.rabbitmq.queue}") String eventQueue,
                          @Value("${spring.rabbitmq.dlq}") String eventDlq,
                          @Value("${spring.rabbitmq.routing-key}") String routingKey,
                          @Value("${spring.rabbitmq.dlq-routing-key}") String dlqRoutingKey) {
        this.EXCHANGE = exchange;
        this.DLX = dlqExchange;
        this.EVENT_QUEUE = eventQueue;
        this.DLQ = eventDlq;
        this.routingKey = routingKey;
        this.dlqRoutingKey = dlqRoutingKey;
    }


    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public DirectExchange dlqExchange() {
        return new DirectExchange(DLX);
    }

    @Bean
    public Queue eventQueue() {
        return QueueBuilder
                .durable(EVENT_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", dlqRoutingKey)
                .build();
    }

    @Bean
    public Queue eventDlq() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    public Binding dlqBinding(Queue eventDlq, DirectExchange dlqExchange) {
        return BindingBuilder
                .bind(eventDlq)
                .to(dlqExchange)
                .with(dlqRoutingKey);
    }

    @Bean
    public Binding eventBinding(Queue eventQueue, DirectExchange directExchange) {
        return BindingBuilder
                .bind(eventQueue)
                .to(directExchange)
                .with(routingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}

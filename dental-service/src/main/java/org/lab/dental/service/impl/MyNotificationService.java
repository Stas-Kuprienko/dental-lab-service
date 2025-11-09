package org.lab.dental.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.lab.dental.service.EmailNotificationService;
import org.lab.dental.service.NotificationService;
import org.lab.dental.util.LetterTemplateKey;
import org.lab.dental.util.LetterTemplateUtility;
import org.lab.event.EventMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class MyNotificationService implements NotificationService {

    private static final String EMAIL_VERIFICATION_PATH = "/main/user/verify?token=";

    private final EmailNotificationService emailNotificationService;
    private final KafkaNotificationService kafkaNotificationService;
    private final LetterTemplateUtility letterTemplateUtility;
    private final String serviceUrl;


    @Autowired
    public MyNotificationService(EmailNotificationService emailNotificationService,
                                 KafkaNotificationService kafkaNotificationService,
                                 LetterTemplateUtility letterTemplateUtility,
                                 @Value("${project.variables.service-url}") String serviceUrl) {
        this.emailNotificationService = emailNotificationService;
        this.kafkaNotificationService = kafkaNotificationService;
        this.letterTemplateUtility = letterTemplateUtility;
        this.serviceUrl = serviceUrl;
    }


    @Override
    public void sendEmailVerifyLink(UUID userId, String email, String data) {
        sendEmailLink(userId, email, data, LetterTemplateKey.EMAIL_VERIFICATION);
    }

    @Override
    public void sendEmailChangeLink(UUID userId, String email, String data) {
        sendEmailLink(userId, email, data, LetterTemplateKey.CHANGE_EMAIL_LINK);
    }

    @Override
    public void sendTelegramMessage(EventMessage message) {
        log.info("The message '{}' is accepted to send to Kafka-service", message.getId());
        kafkaNotificationService
                .send(message)
                .thenAccept(v ->
                        log.info("The message '{}' was sent to Kafka-service successfully", message.getId())
                ).exceptionally(throwable -> {
                    log.error("Failure to send the message '%s' to Kafka-service".formatted(message.getId()), throwable);
                    return null;
                });
    }


    private void sendEmailLink(UUID userId, String email, String data, LetterTemplateKey key) {
        log.info("The token message is accepted to send to the email for user '{}'", userId);
        CompletableFuture.runAsync(() -> {
            Locale locale = Locale.of("RU");
            String link = serviceUrl + EMAIL_VERIFICATION_PATH + data;
            String message = letterTemplateUtility.construct(locale, key, link);
            emailNotificationService.sendHtmlEmail(message, email);
        }).thenAccept(v ->
                log.info("The token message was sent to the email for user email '{}' successfully", email)
        ).exceptionally(throwable -> {
            log.error("Failure to send the token message to the email for user email '%s'".formatted(email),throwable.getMessage());
            return null;
        });
    }
}

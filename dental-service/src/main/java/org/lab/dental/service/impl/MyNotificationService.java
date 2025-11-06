package org.lab.dental.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.lab.dental.entity.EmailVerificationTokenEntity;
import org.lab.dental.entity.UserEntity;
import org.lab.dental.service.EmailNotificationService;
import org.lab.dental.service.NotificationService;
import org.lab.dental.service.UserService;
import org.lab.dental.util.LetterTemplateKey;
import org.lab.dental.util.LetterTemplateUtility;
import org.lab.event.EventMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class MyNotificationService implements NotificationService {

    private static final String EMAIL_VERIFICATION_PATH = "/main/user/verify?token=";

    private final UserService userService;
    private final EmailNotificationService emailNotificationService;
    private final KafkaNotificationService kafkaNotificationService;
    private final LetterTemplateUtility letterTemplateUtility;
    private final String serviceUrl;


    @Autowired
    public MyNotificationService(UserService userService,
                                 EmailNotificationService emailNotificationService,
                                 KafkaNotificationService kafkaNotificationService,
                                 LetterTemplateUtility letterTemplateUtility,
                                 @Value("${project.variables.service-url}") String serviceUrl) {
        this.userService = userService;
        this.emailNotificationService = emailNotificationService;
        this.kafkaNotificationService = kafkaNotificationService;
        this.letterTemplateUtility = letterTemplateUtility;
        this.serviceUrl = serviceUrl;
    }


    @Override
    public void sendEmailVerifyLink(EmailVerificationTokenEntity emailVerificationToken) {
        sendEmailLink(emailVerificationToken, LetterTemplateKey.EMAIL_VERIFICATION);
    }

    @Override
    public void sendEmailChangeLink(EmailVerificationTokenEntity emailVerificationToken) {
        sendEmailLink(emailVerificationToken, LetterTemplateKey.CHANGE_EMAIL_LINK);
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


    private void sendEmailLink(EmailVerificationTokenEntity emailVerificationToken, LetterTemplateKey key) {
        log.info("The token message is accepted to send to the email for user '{}'", emailVerificationToken.getUserId());
        CompletableFuture.runAsync(() -> {
            UserEntity user = userService.getById(emailVerificationToken.getUserId());
            String mail = user.getLogin();
            Locale locale = Locale.of("RU");
            String link = serviceUrl + EMAIL_VERIFICATION_PATH + emailVerificationToken.getToken();
            String message = letterTemplateUtility.construct(locale, key, user.getName(), link);
            emailNotificationService.sendHtmlEmail(message, mail);
        }).thenAccept(v ->
                log.info("The token message was sent to the email for user email '{}' successfully", emailVerificationToken.getEmail())
        ).exceptionally(throwable -> {
            log.error("Failure to send the token message to the email for user email '%s'".formatted(emailVerificationToken.getEmail()),throwable.getMessage());
            return null;
        });
    }
}

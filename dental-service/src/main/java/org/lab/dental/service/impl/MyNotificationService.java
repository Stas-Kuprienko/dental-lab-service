package org.lab.dental.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.lab.dental.configuration.DentalLabConfiguration;
import org.lab.dental.service.EmailNotificationService;
import org.lab.dental.service.EventMessageService;
import org.lab.dental.service.NotificationService;
import org.lab.dental.util.letter.LetterTemplateKeys;
import org.lab.dental.util.letter.LetterTemplateUtility;
import org.lab.event.EventMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
public class MyNotificationService implements NotificationService {

    private static final String EMAIL_VERIFICATION_PATH = "/main/user/verify?token=";
    private static final String RESET_PASSWORD_VERIFICATION_PATH = "/auth/reset-password-verify?email=%s&token=%s";
    private static final Locale DEFAULT_LOCALE = Locale.of("RU");

    private final EmailNotificationService emailNotificationService;
    private final EventMessageService eventMessageService;
    private final LetterTemplateUtility letterTemplateUtility;
    private final String serviceUrl;


    @Autowired
    public MyNotificationService(EmailNotificationService emailNotificationService,
                                 EventMessageService eventMessageService,
                                 LetterTemplateUtility letterTemplateUtility,
                                 @Value("${project.variables.service-url}") String serviceUrl,
                                 @Value("${project.mailing.is-mock:false}") Boolean isMock) {
        if (isMock) {
            this.emailNotificationService = new MockMailSender();
            this.eventMessageService = new MockTelegramSender();
            log.info("NotificationService is configured with mock mailing");
        } else {
            this.emailNotificationService = emailNotificationService;
            this.eventMessageService = eventMessageService;
        }
        this.letterTemplateUtility = letterTemplateUtility;
        this.serviceUrl = serviceUrl;
    }


    @Override
    public void sendEmailVerifyLink(UUID userId, String email, String data) {
        sendEmailLink(userId, email, data, LetterTemplateKeys.EMAIL_VERIFICATION);
    }

    @Override
    public void sendEmailChangeLink(UUID userId, String email, String data) {
        sendEmailLink(userId, email, data, LetterTemplateKeys.CHANGE_EMAIL_LINK);
    }

    @Override
    public void sendResetPasswordLink(String email, String data) {
        log.info("The reset password token message is accepted to send to the email '{}'", email);
        String link = serviceUrl + RESET_PASSWORD_VERIFICATION_PATH.formatted(email, data);
        String message = letterTemplateUtility.construct(DEFAULT_LOCALE, LetterTemplateKeys.RESET_PASSWORD_LINK, link);
        emailNotificationService.sendHtmlEmail(message, email);
    }

    @Override
    public void sendEmailWithWorksForTomorrow(UUID userId, String email, String data) {
        log.info("The message with works for tomorrow is accepted to send to the email '{}' for userID='{}'", email, userId);
        String message;
        String localDateTomorrow = LocalDate.now().plusDays(1).format(DentalLabConfiguration.DATE_FORMATTER);
        if (data == null || data.isEmpty()) {
            message = letterTemplateUtility.construct(DEFAULT_LOCALE, LetterTemplateKeys.WORKS_FOR_TOMORROW_NULL, localDateTomorrow);
        } else {
            message = letterTemplateUtility.construct(DEFAULT_LOCALE, LetterTemplateKeys.WORKS_FOR_TOMORROW, localDateTomorrow, data);
        }
        emailNotificationService.sendHtmlEmail(message, email);
    }

    @Override
    public void sendTelegramMessage(EventMessage message) {
        log.info("The message '{}' is accepted to send to EventMessageService ({})", message.getId(), eventMessageService.getClass().getName());
        eventMessageService.send(message);
    }


    private void sendEmailLink(UUID userId, String email, String data, LetterTemplateKeys key) {
        log.info("The token message is accepted to send to the email for user '{}'", userId);
        String link = serviceUrl + EMAIL_VERIFICATION_PATH + data;
        String message = letterTemplateUtility.construct(DEFAULT_LOCALE, key, link);
        emailNotificationService.sendHtmlEmail(message, email);
    }
}

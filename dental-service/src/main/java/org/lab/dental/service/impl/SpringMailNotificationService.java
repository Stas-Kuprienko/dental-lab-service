package org.lab.dental.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.lab.dental.service.EmailNotificationService;
import org.lab.exception.ApplicationCustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.LockSupport;

@Slf4j
@Service
public class SpringMailNotificationService implements EmailNotificationService {

    private static final int retries = 2;

    private final JavaMailSender mailSender;
    private final ExecutorService executorService;
    private final String serviceLabel;
    private final String serviceEmail;


    @Autowired
    public SpringMailNotificationService(JavaMailSender mailSender,
                                         @Qualifier("virtualThreadPerTaskExecutor") ExecutorService executorService,
                                         @Value("${project.variables.service-label}") String serviceLabel,
                                         @Value("${spring.mail.username}") String serviceEmail) {
        this.mailSender = mailSender;
        this.executorService = executorService;
        this.serviceLabel = serviceLabel;
        this.serviceEmail = serviceEmail;
    }


    @Override
    public void sendMessage(String text, String recipient) {
        log.info("The message is accepted to send to the recipient '{}'", recipient);
        CompletableFuture.runAsync(() -> {
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setTo(recipient);
                    message.setSubject(serviceLabel);
                    message.setText(text);
                    message.setFrom(serviceEmail);
                    mailSender.send(message);
                }, executorService
        ).thenAccept(v ->
                log.info("The message is sent to the email '{}' successfully", recipient)
        ).exceptionally(throwable -> {
            log.error("Failure to send the message to the email '%s', left %d retries".formatted(recipient, retries), throwable.getMessage());
            int delaySeconds = 1;
            LockSupport.parkNanos(Duration.of(delaySeconds, ChronoUnit.SECONDS).toNanos());
            retry(text, recipient, retries, delaySeconds, throwable);
            return null;
        });
    }

    @Override
    public void sendHtmlEmail(String text, String recipient) {
        log.info("The message is accepted to send to the recipient '{}'", recipient);
        MimeMessage message;
        try {
            message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(recipient);
            helper.setSubject(serviceLabel);
            helper.setText(text, true);
            helper.setFrom(serviceEmail);
        } catch (MessagingException e) {
            throw new ApplicationCustomException(e);
        }
        CompletableFuture.runAsync(() -> mailSender.send(message), executorService
        ).thenAccept(v ->
                log.info("The message is sent to the email '{}' successfully", recipient)
        ).exceptionally(throwable -> {
            int delaySeconds = 1;
            LockSupport.parkNanos(Duration.of(delaySeconds, ChronoUnit.SECONDS).toNanos());
            retry(message, recipient, retries, delaySeconds, throwable);
            return null;
        });
    }


    private void retry(String text, String recipient, int retryI, int delaySeconds, Throwable throwable) {
        if (retryI > 0) {
            log.error("Failure to send the message to the email '%s', left %d retries".formatted(recipient, retries), throwable.getMessage());
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(recipient);
                message.setSubject(serviceLabel);
                message.setText(text);
                message.setFrom(serviceEmail);
                mailSender.send(message);
                log.info("The message is sent to the email '{}' successfully", recipient);
            } catch (Throwable thr) {
                delaySeconds++;
                retryI--;
                LockSupport.parkNanos(Duration.of(delaySeconds, ChronoUnit.SECONDS).toNanos());
                retry(text, recipient, retryI, delaySeconds, thr);
            }
        } else {
            log.error("Failure to send the message to the email '%s'!".formatted(recipient), throwable);
        }
    }

    private void retry(MimeMessage message, String recipient, int retryI, int delaySeconds, Throwable throwable) {
        if (retryI > 0) {
            log.warn("Failure to send the message to the email '%s', left %d retries".formatted(recipient, retryI), throwable);
            try {
                mailSender.send(message);
                log.info("The message is sent to the email '{}' successfully", recipient);
            } catch (Throwable thr) {
                delaySeconds++;
                retryI--;
                LockSupport.parkNanos(Duration.of(delaySeconds, ChronoUnit.SECONDS).toNanos());
                retry(message, recipient, retryI, delaySeconds, thr);
            }
        } else {
            log.error("Failure to send the message to the email '%s'!".formatted(recipient), throwable);
        }
    }
}

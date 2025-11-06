package org.lab.dental.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.lab.dental.service.EmailNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class SpringMailNotificationService implements EmailNotificationService {

    private final JavaMailSender mailSender;
    private final String serviceLabel;
    private final String serviceEmail;


    @Autowired
    public SpringMailNotificationService(JavaMailSender mailSender,
                                         @Value("${project.variables.service-label}") String serviceLabel,
                                         @Value("${spring.mail.username}") String serviceEmail) {
        this.mailSender = mailSender;
        this.serviceLabel = serviceLabel;
        this.serviceEmail = serviceEmail;
    }


    @Override
    public void sendMessage(String text, String recipient) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipient);
        message.setSubject(serviceLabel);
        message.setText(text);
        message.setFrom(serviceEmail);
        mailSender.send(message);
    }

    @Override
    public void sendHtmlEmail(String text, String recipient) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(recipient);
            helper.setSubject(serviceLabel);
            helper.setText(text, true);
            helper.setFrom(serviceEmail);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new MailException(e.getMessage(), e) {};
        }
    }
}

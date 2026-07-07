package org.lab.dental.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.lab.dental.service.EmailNotificationService;

@Slf4j
public class MockMailSender implements EmailNotificationService {

    @Override
    public void sendMessage(String text, String recipient) {
        log.info("Email is sent to {}", recipient);
    }

    @Override
    public void sendHtmlEmail(String text, String recipient) {
        log.info("Html Email is sent to {}", recipient);
    }
}

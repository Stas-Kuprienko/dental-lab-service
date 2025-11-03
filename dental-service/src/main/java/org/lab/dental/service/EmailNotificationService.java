package org.lab.dental.service;

public interface EmailNotificationService {

    void sendMessage(String text, String recipient);

    void sendHtmlEmail(String text, String recipient);
}

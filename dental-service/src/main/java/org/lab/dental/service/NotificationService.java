package org.lab.dental.service;

import org.lab.event.EventMessage;
import java.util.UUID;

public interface NotificationService {

    void sendEmailVerifyLink(UUID userId, String email, String data);

    void sendEmailChangeLink(UUID userId, String email, String data);

    void sendResetPasswordLink(String email, String data);

    void sendTelegramMessage(EventMessage message);
}

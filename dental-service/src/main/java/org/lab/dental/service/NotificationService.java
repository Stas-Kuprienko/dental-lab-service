package org.lab.dental.service;

import org.lab.dental.entity.EmailVerificationTokenEntity;
import org.lab.event.EventMessage;

public interface NotificationService {
    void sendEmailVerifyLink(EmailVerificationTokenEntity emailVerificationToken);

    void sendEmailChangeLink(EmailVerificationTokenEntity emailVerificationToken);

    void sendTelegramMessage(EventMessage message);
}

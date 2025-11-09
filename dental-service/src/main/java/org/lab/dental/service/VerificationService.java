package org.lab.dental.service;

import org.lab.dental.entity.EmailVerificationTokenEntity;
import java.util.UUID;

public interface VerificationService {

    void createForUserId(UUID userId, String email, boolean toChange);

    void createTelegramOtpForUserId(UUID userId, String email, long chatId);

    boolean verifyUserEmail(UUID userId, String token);

    boolean verifyForChangeEmail(UUID userId, String token);

    EmailVerificationTokenEntity getByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}

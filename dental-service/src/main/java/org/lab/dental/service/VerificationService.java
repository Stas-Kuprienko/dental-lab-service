package org.lab.dental.service;

import org.lab.dental.entity.EmailVerificationTokenEntity;
import java.util.UUID;

public interface VerificationService {

    EmailVerificationTokenEntity createForUserId(UUID userId, String email);

    boolean verifyUserEmail(UUID userId, String token);

    boolean verifyForChangeEmail(UUID userId, String token);

    EmailVerificationTokenEntity getByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}

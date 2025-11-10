package org.lab.dental.service;

import org.lab.dental.entity.EmailVerificationTokenEntity;
import org.lab.dental.entity.ResetPasswordTokenEntity;

import java.util.UUID;

public interface VerificationService {

    void createForUserId(UUID userId, String email, boolean toChange);

    void createTelegramOtpForUserId(UUID userId, String email, long chatId);

    boolean verifyUserEmail(UUID userId, String token);

    boolean verifyForChangeEmail(UUID userId, String token);

    EmailVerificationTokenEntity getByUserId(UUID userId);

    void deleteByUserId(UUID userId);

    void createResetPasswordToken(String email);

    ResetPasswordTokenEntity getResetPasswordTokenById(String email);

    ResetPasswordTokenEntity getResetPasswordTokenByToken(String token);

    boolean verifyResetPasswordToken(String email, String token);

    boolean isVerifiedResetPasswordToken(String email);

    void deleteResetPasswordToken(String email);
}

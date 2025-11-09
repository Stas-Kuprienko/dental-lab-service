package org.lab.dental.service.impl;

import org.lab.dental.entity.EmailVerificationTokenEntity;
import org.lab.dental.entity.UserEntity;
import org.lab.dental.exception.NotFoundCustomException;
import org.lab.dental.repository.EmailVerificationTokenRepository;
import org.lab.dental.service.CredentialService;
import org.lab.dental.service.NotificationService;
import org.lab.dental.service.UserService;
import org.lab.dental.service.VerificationService;
import org.lab.dental.util.CodeGenerator;
import org.lab.enums.UserStatus;
import org.lab.event.EventMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class MyVerificationService implements VerificationService {

    private static final int TOKEN_LENGTH = 64;
    private static final Duration TOKEN_EXPIRATION = Duration.of(30, ChronoUnit.MINUTES);

    private final CodeGenerator codeGenerator;
    private final NotificationService notificationService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final CredentialService credentialService;
    private final UserService userService;


    @Autowired
    public MyVerificationService(CodeGenerator codeGenerator,
                                 NotificationService notificationService,
                                 EmailVerificationTokenRepository emailVerificationTokenRepository,
                                 CredentialService credentialService,
                                 UserService userService) {
        this.codeGenerator = codeGenerator;
        this.notificationService = notificationService;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.credentialService = credentialService;
        this.userService = userService;
    }


    @Override
    public void createForUserId(UUID userId, String email, boolean toChange) {
        String token = codeGenerator.generateStringCode(TOKEN_LENGTH);
        //TODO token hashing
        String tokenHash = token;
        EmailVerificationTokenEntity emailVerificationToken = EmailVerificationTokenEntity.builder()
                .userId(userId)
                .email(email)
                .token(tokenHash)
                .createdAt(LocalDateTime.now())
                .isVerified(false)
                .build();
        emailVerificationTokenRepository.save(emailVerificationToken);
        if (toChange) {
            notificationService.sendEmailChangeLink(userId, email, token);
        } else {
            notificationService.sendEmailVerifyLink(userId, email, token);
        }
    }

    @Override
    public void createTelegramOtpForUserId(UUID userId, String email, long chatId) {
        String otp = codeGenerator.generateNumericCode(6);
        //TODO token hashing
        String tokenHash = otp;
        EmailVerificationTokenEntity emailVerificationToken = EmailVerificationTokenEntity.builder()
                .userId(userId)
                .email(email)
                .token(otp)
                .createdAt(LocalDateTime.now())
                .isVerified(false)
                .build();
        emailVerificationTokenRepository.save(emailVerificationToken);
        EventMessage message = EventMessage.builder()
                .id(UUID.randomUUID())
                .chatId(chatId)
                .text(otp)
                .createdAt(LocalDateTime.now())
                .build();
        notificationService.sendTelegramMessage(message);
    }

    @Override
    public boolean verifyUserEmail(UUID userId, String token) {
        EmailVerificationTokenEntity verificationToken = getByUserId(userId);
        UserEntity user = userService.getById(userId);
        if (!user.getLogin().equals(verificationToken.getEmail())) {
            throw new IllegalArgumentException("the user's email does not match the email of the token");
        }
        if (verificationToken.isVerified()) {
            throw new IllegalArgumentException("the passed token has already been used");
        }
        if (verificationToken.getToken().equals(token)) {
            emailVerificationTokenRepository.setIsVerified(userId, true);
            try {
                credentialService.verifyEmail(verificationToken.getUserId(), verificationToken.getEmail());
                userService.setStatus(userId, UserStatus.ENABLED);
                emailVerificationTokenRepository.deleteById(userId);
                return true;
            } catch (Exception e) {
                emailVerificationTokenRepository.setIsVerified(userId, false);
                throw e;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean verifyForChangeEmail(UUID userId, String token) {
        EmailVerificationTokenEntity verificationToken = getByUserId(userId);
        if (verificationToken.isVerified()) {
            throw new IllegalArgumentException("the passed token has already been used");
        } else if (LocalDateTime.now().isAfter(verificationToken.getCreatedAt().plus(TOKEN_EXPIRATION))) {
            emailVerificationTokenRepository.deleteById(userId);
            throw new IllegalArgumentException("the passed token has already been expired");
        } else {
            boolean result = verificationToken.getToken().equals(token);
            if (result) {
                emailVerificationTokenRepository.setIsVerified(userId, true);
            }
            return result;
        }
    }

    @Override
    public EmailVerificationTokenEntity getByUserId(UUID userId) {
        return emailVerificationTokenRepository.findById(userId)
                .orElseThrow(() ->
                        NotFoundCustomException.byId(EmailVerificationTokenEntity.class.getSimpleName(), userId));
    }

    @Override
    public void deleteByUserId(UUID userId) {
        emailVerificationTokenRepository.deleteById(userId);
    }
}

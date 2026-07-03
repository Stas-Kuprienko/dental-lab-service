package org.lab.dental.service.impl;

import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.lab.dental.entity.TelegramChatEntity;
import org.lab.dental.entity.UserEntity;
import org.lab.dental.exception.NotFoundCustomException;
import org.lab.dental.mapping.UserConverter;
import org.lab.dental.repository.MailingSubscriptionRepository;
import org.lab.dental.repository.TelegramChatRepository;
import org.lab.dental.repository.UserRepository;
import org.lab.dental.service.CredentialService;
import org.lab.dental.service.UserService;
import org.lab.dental.service.WorkPhotoFileService;
import org.lab.dental.util.metrics.ServiceMetrics;
import org.lab.enums.UserStatus;
import org.lab.exception.ApplicationCustomException;
import org.lab.exception.BadRequestCustomException;
import org.lab.model.TelegramChat;
import org.lab.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class MyUserService implements UserService {

    private final CredentialService credentialService;
    private final UserRepository userRepository;
    private final UserConverter converter;
    private final MailingSubscriptionRepository subscriptionRepository;
    private final TelegramChatRepository telegramChatRepository;
    private final WorkPhotoFileService workPhotoFileService;
    private final ServiceMetrics metrics;


    @Autowired
    public MyUserService(CredentialService credentialService,
                         UserRepository userRepository,
                         UserConverter converter,
                         MailingSubscriptionRepository subscriptionRepository,
                         TelegramChatRepository telegramChatRepository,
                         WorkPhotoFileService workPhotoFileService,
                         ServiceMetrics metrics) {
        this.credentialService = credentialService;
        this.userRepository = userRepository;
        this.converter = converter;
        this.subscriptionRepository = subscriptionRepository;
        this.telegramChatRepository = telegramChatRepository;
        this.workPhotoFileService = workPhotoFileService;
        this.metrics = metrics;
    }


    @Transactional
    @Override
    public User create(String login, String name, String password) {
        UUID id = credentialService.signUp(login, password, name);
        try {
            UserEntity user = UserEntity.builder()
                    .id(id)
                    .login(login)
                    .name(name)
                    .createdAt(LocalDate.now())
                    .status(UserStatus.UNVERIFIED)
                    .build();
            user = userRepository.save(user);
            log.info("Created user: " + user);
            metrics.getUserOkCreations().increment();
            return converter.toDto(user);
        } catch (PersistenceException | DataAccessException e) {
            credentialService.deleteUser(id);
            log.error("Failed user signing up", e);
            metrics.getUserFailedCreations().increment();
            throw new BadRequestCustomException(e);
        }
    }

    @Transactional
    @Override
    public User getById(UUID id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> NotFoundCustomException.byId("User", id));
        log.info("Found user by ID '{}'", id);
        User dto = converter.toDto(user);
        subscriptionRepository.findById(id).ifPresent(s -> {
            dto.setMailingType(s.getType());
            log.info("Found mailing subscription ({}) for user '{}'", s.getType(), id);
        });
        return dto;
    }

    @Override
    public String getEmail(UUID id) {
        String email = userRepository.findLoginById(id)
                .orElseThrow(() -> NotFoundCustomException.byId("User", id));
        log.info("Found user email by ID '{}'", id);
        return email;
    }

    @Override
    public void setStatus(UUID id, UserStatus status) {
        userRepository.updateStatus(id, status.name());
        log.info("For user '{}' updated status '{}'", id, status);
    }

    @Transactional
    @Override
    public void updateName(UUID id, String newName) {
        userRepository.updateName(id, newName);
        credentialService.updateName(id, newName);
        log.info("For user '{}' updated name '{}'", id, newName);
    }

    @Transactional
    @Override
    public void updateLogin(UUID id, String newLogin) {
        UserEntity user = findById(id);
        String oldLogin = user.getLogin();
        credentialService.updateEmail(id, newLogin);
        try {
            userRepository.updateLogin(id, newLogin);
            log.info("For user '{}' updated login '{}'", id, newLogin);
        } catch (PersistenceException e) {
            credentialService.updateEmail(id, oldLogin);
            throw new ApplicationCustomException(e);
        }
    }

    @Override
    public void updatePassword(UUID id, String email, String oldPassword, String newPassword) {
        credentialService.updatePassword(id, email, oldPassword, newPassword);
        log.info("For user '{}' updated password", id);
    }

    @Override
    public void logoutById(UUID userId) {
        credentialService.logout(userId);
        log.info("User '{}' logged out", userId);
    }

    @Transactional
    @Override
    public void delete(UUID id) {
        UserEntity user = findById(id);
        CompletableFuture.runAsync(() -> {
            workPhotoFileService.deleteAllForUserId(id);
            userRepository.deleteById(id);
            try {
                credentialService.deleteUser(id);
            } catch (Exception e) {
                userRepository.save(user);
                metrics.getUserFailedDeletions().increment();
                throw new ApplicationCustomException(e);
            }
        }).thenAccept(v -> {
            log.info("User '{}' is deleted", id);
            metrics.getUserOkDeletions().increment();
        });
    }

    @Transactional
    @Override
    public void addTelegram(UUID id, Long chatId, String language) {
        TelegramChatEntity telegramChat = TelegramChatEntity.builder()
                .userId(id)
                .chatId(chatId)
                .language(language)
                .status(UserStatus.ENABLED)
                .createdAt(LocalDate.now())
                .build();
        telegramChatRepository.deleteByUserId(id);
        telegramChatRepository.save(telegramChat);
        log.info("For user '{}' saved TelegramChat {}", id, chatId);
    }

    @Override
    public TelegramChat getTelegramChat(Long chatId) {
        TelegramChatEntity telegramChat = telegramChatRepository.findById(chatId)
                .orElseThrow(() -> NotFoundCustomException.byId("TelegramChat", chatId));
        log.info("TelegramChat found by chatID '{}'", chatId);
        return converter.telegramChatToDto(telegramChat);
    }

    @Override
    public TelegramChat getTelegramChat(UUID userId) {
        TelegramChatEntity telegramChat = telegramChatRepository.findByUserId(userId)
                .orElseThrow(() -> NotFoundCustomException.byParams("TelegramChat", Map.of("userId", userId)));
        log.info("TelegramChat found by userID '{}'", userId);
        return converter.telegramChatToDto(telegramChat);
    }


    private UserEntity findById(UUID id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> NotFoundCustomException.byId("User", id));
        log.info("Found user by ID '{}'", id);
        return user;
    }
}

package org.lab.dental.service.impl;

import jakarta.persistence.PersistenceException;
import lombok.extern.slf4j.Slf4j;
import org.lab.dental.entity.TelegramChatEntity;
import org.lab.dental.entity.UserEntity;
import org.lab.dental.exception.NotFoundCustomException;
import org.lab.dental.repository.TelegramChatRepository;
import org.lab.dental.repository.UserRepository;
import org.lab.dental.service.CredentialService;
import org.lab.dental.service.UserService;
import org.lab.dental.service.WorkPhotoFileService;
import org.lab.enums.UserStatus;
import org.lab.exception.ApplicationCustomException;
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
    private final TelegramChatRepository telegramChatRepository;
    private final WorkPhotoFileService workPhotoFileService;


    @Autowired
    public MyUserService(CredentialService credentialService,
                         UserRepository userRepository,
                         TelegramChatRepository telegramChatRepository,
                         WorkPhotoFileService workPhotoFileService) {
        this.credentialService = credentialService;
        this.userRepository = userRepository;
        this.telegramChatRepository = telegramChatRepository;
        this.workPhotoFileService = workPhotoFileService;
    }


    @Override
    public UserEntity create(String login, String name, String password) {
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
            return user;
        } catch (PersistenceException | DataAccessException e) {
            credentialService.deleteUser(id);
            throw new ApplicationCustomException(e);
        }
    }

    @Override
    public UserEntity getById(UUID id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> NotFoundCustomException.byId("User", id));
        log.info("Found user by ID '{}'", id);
        return user;
    }

    @Override
    public void setStatus(UUID id, UserStatus status) {
        userRepository.updateStatus(id, status.name());
        log.info("For user '{}' updated status '{}'", id, status);
    }

    @Override
    public void updateName(UUID id, String newName) {
        userRepository.updateName(id, newName);
        credentialService.updateName(id, newName);
        log.info("For user '{}' updated name '{}'", id, newName);
    }

    @Override
    public void updateLogin(UUID id, String newLogin) {
        UserEntity user = getById(id);
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

    @Override
    public void delete(UUID id) {
        UserEntity user = getById(id);
        CompletableFuture.runAsync(() -> {
            workPhotoFileService.deleteAllForUserId(id);
            userRepository.deleteById(id);
            try {
                credentialService.deleteUser(id);
            } catch (Exception e) {
                userRepository.save(user);
                throw new ApplicationCustomException(e);
            }
        }).thenAccept(v -> log.info("User '{}' is deleted", id));
    }

    @Override
    public void addTelegram(UUID id, Long chatId) {
        TelegramChatEntity telegramChat = TelegramChatEntity.builder()
                .userId(id)
                .chatId(chatId)
                .status(UserStatus.ENABLED)
                .createdAt(LocalDate.now())
                .build();
        telegramChatRepository.save(telegramChat);
        log.info("For user '{}' saved TelegramChat {}", id, chatId);
    }

    @Override
    public TelegramChatEntity getTelegramChat(Long chatId) {
        TelegramChatEntity telegramChat = telegramChatRepository.findById(chatId)
                .orElseThrow(() -> NotFoundCustomException.byId("TelegramChat", chatId));
        log.info("TelegramChat found by chatID '{}'", chatId);
        return telegramChat;
    }

    @Override
    public TelegramChatEntity getTelegramChat(UUID userId) {
        TelegramChatEntity telegramChat = telegramChatRepository.findByUserId(userId)
                .orElseThrow(() -> NotFoundCustomException.byParams("TelegramChat", Map.of("userId", userId)));
        log.info("TelegramChat found by userID '{}'", userId);
        return telegramChat;
    }
}

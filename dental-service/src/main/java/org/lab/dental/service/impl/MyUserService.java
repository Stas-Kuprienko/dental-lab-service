package org.lab.dental.service.impl;

import jakarta.persistence.PersistenceException;
import org.lab.dental.entity.TelegramChatEntity;
import org.lab.dental.entity.UserEntity;
import org.lab.dental.exception.NotFoundCustomException;
import org.lab.dental.repository.TelegramChatRepository;
import org.lab.dental.repository.UserRepository;
import org.lab.dental.service.CredentialService;
import org.lab.dental.service.UserService;
import org.lab.enums.UserStatus;
import org.lab.exception.InternalCustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Service
public class MyUserService implements UserService {

    private final CredentialService credentialService;
    private final UserRepository userRepository;
    private final TelegramChatRepository telegramChatRepository;


    @Autowired
    public MyUserService(CredentialService credentialService,
                         UserRepository userRepository,
                         TelegramChatRepository telegramChatRepository) {
        this.credentialService = credentialService;
        this.userRepository = userRepository;
        this.telegramChatRepository = telegramChatRepository;
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
            return user;
        } catch (PersistenceException | DataAccessException e) {
            credentialService.deleteUser(id);
            throw new InternalCustomException(e);
        }
    }

    @Override
    public UserEntity getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> NotFoundCustomException.byId("User", id));
    }

    @Override
    public void setStatus(UUID id, UserStatus status) {
        userRepository.updateStatus(id, status.name());
    }

    @Override
    public void updateName(UUID id, String newName) {
        userRepository.updateName(id, newName);
        credentialService.updateName(id, newName);
    }

    @Override
    public void updateLogin(UUID id, String newLogin) {
        UserEntity user = getById(id);
        String oldLogin = user.getLogin();
        credentialService.updateEmail(id, newLogin);
        try {
            userRepository.updateLogin(id, newLogin);
        } catch (PersistenceException e) {
            credentialService.updateEmail(id, oldLogin);
            throw new InternalCustomException(e);
        }
    }

    @Override
    public void updatePassword(UUID id, String email, String oldPassword, String newPassword) {
        credentialService.updatePassword(id, email, oldPassword, newPassword);
    }

    @Override
    public void logoutById(UUID userId) {
        credentialService.logout(userId);
    }

    @Override
    public void delete(UUID id) {
        UserEntity user = getById(id);
        userRepository.deleteById(id);
        try {
            credentialService.deleteUser(id);
        } catch (Exception e) {
            userRepository.save(user);
            throw new InternalCustomException(e);
        }
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
    }

    @Override
    public TelegramChatEntity getTelegramChat(Long chatId) {
        return telegramChatRepository.findById(chatId)
                .orElseThrow(() -> NotFoundCustomException.byId("TelegramChat", chatId));
    }

    @Override
    public TelegramChatEntity getTelegramChat(UUID userId) {
        return telegramChatRepository.findByUserId(userId)
                .orElseThrow(() -> NotFoundCustomException.byParams("TelegramChat", Map.of("userId", userId)));
    }
}

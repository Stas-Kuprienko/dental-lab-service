package org.lab.dental.service;

import org.lab.dental.entity.TelegramChatEntity;
import org.lab.dental.entity.UserEntity;
import org.lab.enums.UserStatus;
import java.util.UUID;

public interface UserService {

    UserEntity create(String login, String name, String password);

    UserEntity getById(UUID id);

    void setStatus(UUID id, UserStatus status);

    UserEntity updateName(UUID id, String name);

    UserEntity updateLogin(UUID id, String login);

    void updatePassword(UUID id, String email, String oldPassword, String newPassword);

    void delete(UUID id);

    void addTelegram(UUID id, Long chatId);

    TelegramChatEntity getTelegramChat(Long chatId);

    TelegramChatEntity getTelegramChat(UUID userId);
}

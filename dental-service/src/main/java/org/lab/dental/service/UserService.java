package org.lab.dental.service;

import org.lab.enums.UserStatus;
import org.lab.model.TelegramChat;
import org.lab.model.User;
import java.util.UUID;

public interface UserService {

    User create(String login, String name, String password);

    User getById(UUID id);

    void setStatus(UUID id, UserStatus status);

    void updateName(UUID id, String name);

    void updateLogin(UUID id, String login);

    void updatePassword(UUID id, String email, String oldPassword, String newPassword);

    void logoutById(UUID userId);

    void delete(UUID id);

    void addTelegram(UUID id, Long chatId, String language);

    TelegramChat getTelegramChat(Long chatId);

    TelegramChat getTelegramChat(UUID userId);
}

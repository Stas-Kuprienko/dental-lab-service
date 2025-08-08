package org.lab.dental.service.impl;

import org.lab.dental.entity.TelegramChatEntity;
import org.lab.dental.entity.UserEntity;
import org.lab.dental.exception.NotFoundCustomException;
import org.lab.dental.repository.TelegramChatRepository;
import org.lab.dental.repository.UserRepository;
import org.lab.dental.service.CredentialService;
import org.lab.dental.service.UserService;
import org.lab.enums.UserStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class MyUserService implements UserService {

    private final UserRepository userRepository;
    private final CredentialService credentialService;
    private final TelegramChatRepository telegramChatRepository;


    @Autowired
    public MyUserService(UserRepository userRepository,
                         CredentialService credentialService,
                         TelegramChatRepository telegramChatRepository) {
        this.userRepository = userRepository;
        this.credentialService = credentialService;
        this.telegramChatRepository = telegramChatRepository;
    }


    @Override
    public UserEntity create(String login, String password, String name) {
        UUID id = credentialService.signUp(login, password, name);
        try {
            UserEntity user = UserEntity.builder()
                    .id(id)
                    .login(login)
                    .name(name)
                    .createdAt(LocalDate.now())
                    .status(UserStatus.ENABLED)
                    .build();
            return userRepository.save(user);
        } catch (Exception e) {
            //TODO
            credentialService.deleteUser(login);
            throw e;
        }
    }

    @Override
    public UserEntity getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> NotFoundCustomException.byId("User", id));
    }

    @Override
    public UserEntity updateName(UUID id, String name) {
        userRepository.updateName(id, name);
        return getById(id);
    }

    @Override
    public void delete(UUID id) {
        userRepository.deleteById(id);
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
}

package org.lab.dental.service.impl;

import org.lab.dental.entity.UserEntity;
import org.lab.dental.exception.NotFoundCustomException;
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

    @Autowired
    public MyUserService(UserRepository userRepository, CredentialService credentialService) {
        this.userRepository = userRepository;
        this.credentialService = credentialService;
    }


    @Override
    public UserEntity create(String login, String password, String name) {
        UUID id = credentialService.signUp(login, password);
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
}

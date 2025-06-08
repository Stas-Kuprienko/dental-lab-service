package org.lab.dental.service.impl;

import org.lab.dental.entity.UserEntity;
import org.lab.dental.exception.NotFoundCustomException;
import org.lab.dental.exception.PersistenceCustomException;
import org.lab.dental.repository.UserRepository;
import org.lab.dental.service.UserService;
import org.lab.enums.UserStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class MyUserService implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public MyUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserEntity create(UserEntity user) {
        if (user.getId() != null) {
            throw PersistenceCustomException.saveEntityWithId(user);
        }
        user.setCreatedAt(LocalDate.now());
        user.setStatus(UserStatus.ENABLED);
        return userRepository.save(user);
    }

    @Override
    public UserEntity getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> NotFoundCustomException.byId("User", id));
    }
}

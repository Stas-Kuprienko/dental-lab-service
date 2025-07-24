package org.lab.dental.service;

import org.lab.dental.entity.UserEntity;
import java.util.UUID;

public interface UserService {

    UserEntity create(String login, String password, String name);

    UserEntity getById(UUID id);

    UserEntity updateName(UUID id, String name);

    void delete(UUID id);
}

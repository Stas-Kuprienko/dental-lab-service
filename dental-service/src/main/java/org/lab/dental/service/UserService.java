package org.lab.dental.service;

import org.lab.dental.entity.UserEntity;
import java.util.UUID;

public interface UserService {

    UserEntity create(UserEntity user);

    UserEntity getById(UUID id);
}

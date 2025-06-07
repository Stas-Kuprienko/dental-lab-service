package org.lab.dental.service;

import org.lab.dental.entity.UserEntity;
import java.util.UUID;

public interface UserService {

    UserEntity save(UserEntity user);

    UserEntity getById(UUID id);
}

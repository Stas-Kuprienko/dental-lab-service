package org.lab.dental.mapping;

import org.lab.dental.entity.UserEntity;
import org.lab.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserConverter {


    public UserEntity toEntity(User dto) {
        return UserEntity.builder()
                .id(dto.getId())
                .login(dto.getLogin())
                .name(dto.getName())
                .createdAt(dto.getCreatedAt())
                .status(dto.getStatus())
                .build();
    }

    public User toDto(UserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .login(entity.getLogin())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .status(entity.getStatus())
                .build();
    }
}

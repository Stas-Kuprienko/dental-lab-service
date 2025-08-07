package org.lab.dental.repository;

import org.lab.dental.entity.TelegramChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TelegramChatRepository extends JpaRepository<TelegramChatEntity, Long> {


    Optional<TelegramChatEntity> findByUserId(UUID userId);
}

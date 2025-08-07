package org.lab.dental.repository;

import org.lab.dental.entity.TelegramOtpLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TelegramOtpLinkRepository extends JpaRepository<TelegramOtpLinkEntity, String> {


    @Query(value = """
            SELECT tl
            FROM dental_lab.telegram_otp_link tl
            WHERE tl.key = :key
            AND tl.user_id = :userId
            AND tl.chat_id = :chatId
            """, nativeQuery = true)
    Optional<TelegramOtpLinkEntity> findByKeyAndUserIdAndChatId(@Param("key") String key,
                                                                @Param("userId") UUID userId,
                                                                @Param("chatId") Long chatId);
}

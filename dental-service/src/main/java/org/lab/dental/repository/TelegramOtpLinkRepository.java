package org.lab.dental.repository;

import jakarta.transaction.Transactional;
import org.lab.dental.entity.TelegramOtpLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TelegramOtpLinkRepository extends JpaRepository<TelegramOtpLinkEntity, String> {


    Optional<TelegramOtpLinkEntity> findByKey(@Param("key") String key);


    @Modifying
    @Transactional
    @Query(value = "UPDATE dental_lab.telegram_otp_link SET user_id = :userId WHERE key = :key", nativeQuery = true)
    void setUserId(@Param("key") String key,
                   @Param("userId") UUID userId);

}

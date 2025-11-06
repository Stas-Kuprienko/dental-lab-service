package org.lab.dental.repository;

import jakarta.transaction.Transactional;
import org.lab.dental.entity.EmailVerificationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationTokenEntity, UUID> {


    @Modifying
    @Transactional
    @Query(value = "UPDATE dental_lab.email_verification_token SET is_verified = :isVerified WHERE user_id = :userId", nativeQuery = true)
    void setIsVerified(@Param("userId") UUID userId,
                       @Param("isVerified") boolean isVerified);
}

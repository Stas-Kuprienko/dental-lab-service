package org.lab.dental.repository;

import jakarta.transaction.Transactional;
import org.lab.dental.entity.ResetPasswordTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResetPasswordTokenRepository extends JpaRepository<ResetPasswordTokenEntity, String> {


    Optional<ResetPasswordTokenEntity> findByToken(@Param("token") String token);


    @Query(value = "SELECT rpt.is_verified FROM dental_lab.reset_password_token rpt WHERE email = :email", nativeQuery = true)
    boolean isVerified(@Param("email") String email);


    @Modifying
    @Transactional
    @Query(value = "UPDATE dental_lab.reset_password_token SET is_verified = :isVerified WHERE email = :email", nativeQuery = true)
    void setIsVerified(@Param("email") String email,
                       @Param("isVerified") boolean isVerified);
}

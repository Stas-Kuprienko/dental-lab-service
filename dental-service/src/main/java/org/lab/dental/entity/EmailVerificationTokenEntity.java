package org.lab.dental.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Builder
@AllArgsConstructor
@Table(name = "email_verification_token", schema = "dental_lab")
public class EmailVerificationTokenEntity {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "email")
    private String email;

    @Column(name = "token")
    private String token;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "is_verified")
    private boolean isVerified;


    public EmailVerificationTokenEntity() {}
}

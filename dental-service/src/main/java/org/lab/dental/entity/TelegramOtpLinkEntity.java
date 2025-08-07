package org.lab.dental.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Builder
@AllArgsConstructor
@Table(name = "telegram_otp_link", schema = "dental_lab")
public class TelegramOtpLinkEntity {

    @Id
    @Column(name = "key")
    private String key;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "otp")
    private String otp;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;


    public TelegramOtpLinkEntity() {}
}

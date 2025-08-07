package org.lab.dental.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.lab.enums.UserStatus;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Builder
@Getter @Setter
@AllArgsConstructor
@Table(name = "telegram_chat", schema = "dental_lab")
public class TelegramChatEntity {

    @Id
    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "status")
    @Enumerated(value = EnumType.STRING)
    private UserStatus status;

    @Column(name = "created_at")
    private LocalDate createdAt;


    public TelegramChatEntity() {}


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TelegramChatEntity that)) return false;
        return Objects.equals(chatId, that.chatId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatId);
    }

    @Override
    public String toString() {
        return "TelegramChatEntity{" +
                "chatId=" + chatId +
                ", userId=" + userId +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
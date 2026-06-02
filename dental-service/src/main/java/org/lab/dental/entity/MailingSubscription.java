package org.lab.dental.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.lab.enums.MailingType;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "mailing_subscription", schema = "dental_lab")
@Builder
@Getter @Setter
@AllArgsConstructor
public class MailingSubscription {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private MailingType type;

    @Column(name = "created_at")
    private LocalDate createdAt;


    public MailingSubscription() {}


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MailingSubscription that)) return false;
        return Objects.equals(userId, that.userId) && type == that.type && Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, type, createdAt);
    }
}

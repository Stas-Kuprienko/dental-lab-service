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
@Table(name = "users", schema = "dental_lab")
@Builder
@Getter @Setter
@AllArgsConstructor
public class UserEntity {

    @Id
    private UUID id;

    @Column(name = "login")
    private String login;

    @Column(name = "name")
    private String name;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "status")
    @Enumerated(value = EnumType.STRING)
    private UserStatus status;


    public UserEntity() {}


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserEntity that)) return false;
        return Objects.equals(login, that.login) &&
                Objects.equals(name, that.name) &&
                Objects.equals(createdAt, that.createdAt) &&
                status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, name, createdAt, status);
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", name='" + name + '\'' +
                ", createdAt=" + createdAt +
                ", status=" + status +
                '}';
    }
}

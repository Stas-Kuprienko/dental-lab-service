package org.lab.dental.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.lab.enums.WorkStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "dental_work", schema = "dental_lab")
@Builder
@Getter @Setter
@AllArgsConstructor
public class DentalWorkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clinic")
    private String clinic;

    @Column(name = "patient")
    private String patient;

    @Column(name = "accepted_at")
    private LocalDate acceptedAt;

    @Column(name = "complete_at")
    private LocalDate completeAt;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private WorkStatus status;

    @Column(name = "comment")
    private String comment;

    @Column(name = "user_id")
    private UUID userId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "dental_work_id")
    private List<ProductEntity> products;


    public DentalWorkEntity() {}


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DentalWorkEntity that)) return false;
        return Objects.equals(clinic, that.clinic) &&
                Objects.equals(patient, that.patient) &&
                Objects.equals(acceptedAt, that.acceptedAt) &&
                Objects.equals(completeAt, that.completeAt) &&
                status == that.status &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(products, that.products);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clinic, patient, acceptedAt, completeAt, status, userId, products);
    }

    @Override
    public String toString() {
        return "DentalWorkEntity{" +
                "id=" + id +
                ", clinic='" + clinic + '\'' +
                ", patient='" + patient + '\'' +
                ", acceptedAt=" + acceptedAt +
                ", completeAt=" + completeAt +
                ", status=" + status +
                ", comment='" + comment + '\'' +
                ", userId=" + userId +
                ", products=" + products +
                '}';
    }
}

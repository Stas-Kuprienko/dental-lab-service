package org.lab.dental.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "product", schema = "dental_lab")
@Builder
@Getter @Setter
@AllArgsConstructor
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "title")
    private String title;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "dental_work_id")
    private Long dentalWorkId;

    @Column(name = "accepted_at")
    private LocalDate acceptedAt;

    @Column(name = "complete_at")
    private LocalDate completeAt;


    public ProductEntity() {}


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductEntity product)) return false;
        return Objects.equals(title, product.title) &&
                Objects.equals(price, product.price) &&
                Objects.equals(quantity, product.quantity) &&
                Objects.equals(dentalWorkId, product.dentalWorkId) &&
                Objects.equals(acceptedAt, product.acceptedAt) &&
                Objects.equals(completeAt, product.completeAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, price, quantity, dentalWorkId, acceptedAt, completeAt);
    }

    @Override
    public String toString() {
        return "ProductEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", dentalWorkId=" + dentalWorkId +
                ", acceptedAt=" + acceptedAt +
                ", completeAt=" + completeAt +
                '}';
    }
}

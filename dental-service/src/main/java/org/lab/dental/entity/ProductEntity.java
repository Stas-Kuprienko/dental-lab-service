package org.lab.dental.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
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


    public ProductEntity() {}


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductEntity that)) return false;
        return Objects.equals(title, that.title) &&
                Objects.equals(price, that.price) &&
                Objects.equals(quantity, that.quantity) &&
                Objects.equals(dentalWorkId, that.dentalWorkId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, price, quantity, dentalWorkId);
    }

    @Override
    public String toString() {
        return "ProductEntity{" +
                "id=" + id +
                ", productType='" + title + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", dentalWorkId=" + dentalWorkId +
                '}';
    }
}

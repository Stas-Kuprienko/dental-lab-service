package org.lab.dental.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "product_type", schema = "dental_lab")
@Builder
@Getter @Setter
@AllArgsConstructor
public class ProductTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "title")
    private String title;

    @Column(name = "price")
    private double price;

    @Column(name = "user_id")
    private UUID userId;


    public ProductTypeEntity() {}


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductTypeEntity that)) return false;
        return Objects.equals(title, that.title) &&
                Objects.equals(price, that.price) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, price, userId);
    }

    @Override
    public String toString() {
        return "ProductTypeEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", userId=" + userId +
                '}';
    }
}

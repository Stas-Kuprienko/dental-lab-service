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

    @Column(name = "product_type")
    private String productType;

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
        return Objects.equals(productType, that.productType) &&
                Objects.equals(price, that.price) &&
                Objects.equals(quantity, that.quantity) &&
                Objects.equals(dentalWorkId, that.dentalWorkId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productType, price, quantity, dentalWorkId);
    }

    @Override
    public String toString() {
        return "ProductEntity{" +
                "id=" + id +
                ", productType='" + productType + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", dentalWorkId=" + dentalWorkId +
                '}';
    }
}

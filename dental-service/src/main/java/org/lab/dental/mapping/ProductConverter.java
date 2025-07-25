package org.lab.dental.mapping;

import org.lab.dental.entity.ProductEntity;
import org.lab.model.Product;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class ProductConverter {


    public ProductEntity toEntity(Product dto) {
        return ProductEntity.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .quantity(dto.getQuantity())
                .price(BigDecimal.valueOf(dto.getPrice()))
                .acceptedAt(dto.getAcceptedAt())
                .completeAt(dto.getCompleteAt())
                .dentalWorkId(dto.getDentalWorkId())
                .build();
    }

    public Product toDto(ProductEntity entity) {
        return Product.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .quantity(entity.getQuantity())
                .price(entity.getPrice().floatValue())
                .acceptedAt(entity.getAcceptedAt())
                .completeAt(entity.getCompleteAt())
                .dentalWorkId(entity.getDentalWorkId())
                .build();
    }
}

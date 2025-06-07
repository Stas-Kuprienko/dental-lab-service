package org.lab.dental.mapping;

import org.lab.dental.entity.ProductTypeEntity;
import org.lab.dto.ProductMap;
import org.lab.dto.ProductType;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
public class ProductTypeConverter {


    public ProductTypeEntity toEntity(ProductType dto, UUID userId) {
        return ProductTypeEntity.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .price(BigDecimal.valueOf(dto.getPrice()))
                .userId(userId)
                .build();
    }

    public ProductType toDto(ProductTypeEntity entity) {
        return ProductType.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .price(entity.getPrice().floatValue())
                .build();
    }

    public ProductMap toProductMap(List<ProductTypeEntity> entities) {
        UUID userId = entities.getFirst().getUserId();
        List<ProductType> productTypes = entities
                .stream()
                .map(entity -> {
                    if (!entity.getUserId().equals(userId)) {
                        throw new IllegalArgumentException("List of ProductTypeEntities incorrect - items has different 'userId'.\n" + entities);
                    }
                    return toDto(entity);
                })
                .toList();
        return new ProductMap(userId, productTypes);
    }

    public List<ProductTypeEntity> fromProductMap(ProductMap productMap) {
        return productMap
                .getEntries()
                .stream()
                .map(productType -> toEntity(productType, productMap.getUserId()))
                .toList();
    }
}

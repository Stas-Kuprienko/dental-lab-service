package org.lab.dental.mapping;

import org.lab.dental.entity.ProductTypeEntity;
import org.lab.model.ProductMap;
import org.lab.model.ProductType;
import org.lab.request.NewProductType;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public ProductTypeEntity fromRequest(NewProductType newProductType, UUID userId) {
        return ProductTypeEntity.builder()
                .title(newProductType.getTitle())
                .price(BigDecimal.valueOf(newProductType.getPrice()))
                .userId(userId)
                .build();
    }

    public ProductMap toProductMap(UUID userId, List<ProductTypeEntity> entities) {
        List<ProductType> productTypes = entities
                .stream()
                .map(entity -> {
                    if (!entity.getUserId().equals(userId)) {
                        throw new IllegalArgumentException("List of ProductTypeEntities incorrect - items has different 'userId'.\n" + entities);
                    }
                    return toDto(entity);
                })
                .collect(Collectors.toList());
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

package org.lab.dental.service;

import org.lab.dental.entity.ProductTypeEntity;

import java.util.List;
import java.util.UUID;

public interface ProductTypeService {
    ProductTypeEntity create(ProductTypeEntity productType);

    ProductTypeEntity getByIdAndUserId(UUID id, UUID userId);

    List<ProductTypeEntity> getAllByUserId(UUID userId);

    ProductTypeEntity update(ProductTypeEntity updatable);

    void delete(UUID id, UUID userId);
}

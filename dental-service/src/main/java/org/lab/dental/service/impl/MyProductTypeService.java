package org.lab.dental.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.lab.dental.entity.ProductTypeEntity;
import org.lab.dental.exception.NotFoundCustomException;
import org.lab.dental.exception.PersistenceCustomException;
import org.lab.dental.repository.ProductTypeRepository;
import org.lab.dental.service.ProductTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class MyProductTypeService implements ProductTypeService {

    private final ProductTypeRepository repository;

    @Autowired
    public MyProductTypeService(ProductTypeRepository repository) {
        this.repository = repository;
    }


    @Override
    public ProductTypeEntity create(ProductTypeEntity productType) {
        if (productType.getId() != null) {
            throw PersistenceCustomException.saveEntityWithId(productType);
        }
        log.info("Entity received to save: {}", productType);
        ProductTypeEntity saved = repository.save(productType);
        log.info("Entity saved: {}", saved);
        return saved;
    }

    @Override
    public ProductTypeEntity getByIdAndUserId(UUID id, UUID userId) {
        ProductTypeEntity productType = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> NotFoundCustomException
                        .byParams("ProductType", Map.of("id", id, "userId", userId)));
        log.info("Entity is found: {}", productType);
        return productType;
    }

    @Override
    public List<ProductTypeEntity> getAllByUserId(UUID userId) {
        List<ProductTypeEntity> productTypes = repository.findAllByUserId(userId);
        log.info("Found {} ProductTypes by parameters: userId='{}'", productTypes.size(), userId);
        return productTypes;
    }

    @Override
    public void update(UUID id, UUID userId, float newPrice) {
        repository.updatePrice(id, userId, BigDecimal.valueOf(newPrice));
        log.info("ProductType with ID={} and userID='{}' is updated", id, userId);
    }

    @Override
    public void delete(UUID id, UUID userId) {
        repository.deleteByIdAndUserId(id, userId);
        log.info("ProductType with ID={} and userID='{}' is deleted", id, userId);
    }
}

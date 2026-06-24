package org.lab.dental.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.lab.dental.entity.ProductTypeEntity;
import org.lab.dental.exception.NotFoundCustomException;
import org.lab.dental.exception.PersistenceCustomException;
import org.lab.dental.mapping.ProductTypeConverter;
import org.lab.dental.repository.ProductTypeRepository;
import org.lab.dental.repository.redis.RedisProductMapRepository;
import org.lab.dental.service.ProductTypeService;
import org.lab.model.ProductMap;
import org.lab.model.ProductType;
import org.lab.request.NewProductType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class MyProductTypeService implements ProductTypeService {

    private final ProductTypeRepository repository;
    private final RedisProductMapRepository redisRepository;
    private final ProductTypeConverter converter;


    @Autowired
    public MyProductTypeService(ProductTypeRepository repository,
                                RedisProductMapRepository redisRepository,
                                ProductTypeConverter converter) {
        this.repository = repository;
        this.redisRepository = redisRepository;
        this.converter = converter;
    }


    @Override
    public ProductMap create(NewProductType newProductType, UUID userId) {
        ProductTypeEntity productType = converter.fromRequest(newProductType, userId);
        if (productType.getId() != null) {
            throw PersistenceCustomException.saveEntityWithId(productType);
        }
        log.info("Entity received to save: {}", productType);
        ProductTypeEntity saved = repository.save(productType);
        log.info("Entity saved: {}", saved);
        ProductType type = converter.toDto(saved);
        Optional<ProductMap> optional = redisRepository.save(type, saved.getUserId());
        if (optional.isPresent()) {
            return optional.get();
        } else {
            ProductMap map = converter.toProductMap(userId, repository.findAllByUserId(userId));
            redisRepository.save(map);
            return map;
        }
    }

    @Override
    public ProductType getByIdAndUserId(UUID id, UUID userId) {
        Optional<ProductMap> optional = redisRepository.get(userId);
        if (optional.isPresent()) {
            for (ProductType type : optional.get().getEntries()) {
                if (type.getId().equals(id)) return type;
            }
            throw NotFoundCustomException
                    .byParams("ProductType", Map.of("id", id, "userId", userId));
        } else {
            ProductTypeEntity typeEntity = repository.findByIdAndUserId(id, userId)
                    .orElseThrow(() -> NotFoundCustomException
                            .byParams("ProductType", Map.of("id", id, "userId", userId)));
            log.info("Entity is found: {}", typeEntity);
            ProductType productType = converter.toDto(typeEntity);
            redisRepository.save(productType, userId);
            return productType;
        }
    }

    @Override
    public ProductMap getAllByUserId(UUID userId) {
        Optional<ProductMap> optional = redisRepository.get(userId);
        if (optional.isPresent()) {
            return optional.get();
        } else {
            List<ProductTypeEntity> productTypes = repository.findAllByUserId(userId);
            log.info("Found {} ProductTypes by parameters: userId='{}'", productTypes.size(), userId);
            ProductMap map = converter.toProductMap(userId, productTypes);
            redisRepository.save(map);
            return map;
        }
    }

    @Override
    public void update(UUID id, UUID userId, float newPrice) {
        repository.updatePrice(id, userId, BigDecimal.valueOf(newPrice));
        redisRepository.updateIfContains(id, newPrice, userId);
        log.info("ProductType with ID={} and userID='{}' is updated", id, userId);
    }

    @Override
    public void delete(UUID id, UUID userId) {
        repository.deleteByIdAndUserId(id, userId);
        redisRepository.delete(id, userId);
        log.info("ProductType with ID={} and userID='{}' is deleted", id, userId);
    }
}

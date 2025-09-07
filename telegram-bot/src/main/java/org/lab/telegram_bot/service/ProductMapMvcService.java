package org.lab.telegram_bot.service;

import org.lab.exception.NotFoundCustomException;
import org.lab.model.ProductMap;
import org.lab.model.ProductType;
import org.lab.request.NewProductType;
import org.lab.telegram_bot.datasource.ProductMapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductMapMvcService {

    private final ProductMapServiceWrapper productMapService;
    private final ProductMapRepository productMapRepository;

    @Autowired
    public ProductMapMvcService(DentalLabRestClientWrapper dentalLabRestClient, ProductMapRepository productMapRepository) {
        this.productMapService = dentalLabRestClient.PRODUCT_MAP;
        this.productMapRepository = productMapRepository;
    }


    public ProductMap create(NewProductType newProductType, UUID userId) {
        ProductType productType = productMapService.create(newProductType, userId);
        ProductMap map = productMapRepository.get(userId).orElse(productMapService.findAll(userId));
        map.getEntries().add(productType);
        productMapRepository.save(map);
        return map;
    }

    public ProductMap get(UUID userId) {
        Optional<ProductMap> optionalProductMap = productMapRepository.get(userId);
        if (optionalProductMap.isEmpty()) {
            ProductMap map = productMapService.findAll(userId);
            productMapRepository.save(map);
            return map;
        } else {
            return optionalProductMap.get();
        }
    }

    public ProductMap update(UUID userId, UUID productTypeId, float newPrice) {
        productMapService.updatePrice(productTypeId, newPrice, userId);
        ProductMap map = productMapRepository.get(userId).orElseThrow(() -> new NotFoundCustomException("ProductMap is not found for user " + userId));
        ProductType productType = map.get(productTypeId).orElseThrow(() -> new NotFoundCustomException("ProductType '%s' is not found for user '%s'".formatted(productTypeId, userId)));
        productType.setPrice(newPrice);
        productMapRepository.save(map);
        return map;
    }

    public ProductMap delete(UUID userId, UUID productType) {
        productMapService.delete(productType, userId);
        ProductMap map = productMapRepository.get(userId).orElseThrow(() -> new NotFoundCustomException("ProductMap is not found for user " + userId));
        if (map.delete(productType)) {
            productMapRepository.save(map);
        }
        return map;
    }
}

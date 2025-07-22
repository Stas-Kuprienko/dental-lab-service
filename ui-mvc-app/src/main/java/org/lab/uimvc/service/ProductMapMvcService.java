package org.lab.uimvc.service;

import org.dental.restclient.DentalLabRestClient;
import org.dental.restclient.ProductMapService;
import org.lab.exception.NotFoundCustomException;
import org.lab.model.ProductMap;
import org.lab.model.ProductType;
import org.lab.request.NewProductType;
import org.lab.uimvc.datasource.redis.ProductMapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductMapMvcService {

    private final ProductMapService productMapService;
    private final ProductMapRepository productMapRepository;

    @Autowired
    public ProductMapMvcService(DentalLabRestClient dentalLabRestClient, ProductMapRepository productMapRepository) {
        this.productMapService = dentalLabRestClient.PRODUCT_MAP;
        this.productMapRepository = productMapRepository;
    }


    public ProductMap create(UUID userId, NewProductType newProductType) {
        ProductType productType = productMapService.create(newProductType);
        ProductMap map = productMapRepository.get(userId).orElseThrow(() -> new NotFoundCustomException("ProductMap is not found for user '%s'".formatted(userId)));
        map.getEntries().add(productType);
        productMapRepository.save(map);
        return map;
    }

    public ProductMap get(UUID userId) {
        Optional<ProductMap> optionalProductMap = productMapRepository.get(userId);
        if (optionalProductMap.isEmpty()) {
            ProductMap map = productMapService.findAll();
            productMapRepository.save(map);
            return map;
        } else {
            return optionalProductMap.get();
        }
    }

    public ProductMap update(UUID userId, UUID productTypeId, float newPrice) {
        productMapService.updateProductType(productTypeId, newPrice);
        ProductMap map = productMapRepository.get(userId).orElseThrow(() -> new NotFoundCustomException("ProductMap is not found for user " + userId));
        ProductType productType = map.get(productTypeId).orElseThrow(() -> new NotFoundCustomException("ProductType '%s' is not found for user '%s'".formatted(productTypeId, userId)));
        productType.setPrice(newPrice);
        productMapRepository.save(map);
        return map;
    }

    public ProductMap delete(UUID userId, UUID productType) {
        productMapService.delete(productType);
        ProductMap map = productMapRepository.get(userId).orElseThrow(() -> new NotFoundCustomException("ProductMap is not found for user " + userId));
        if (map.delete(productType)) {
            productMapRepository.save(map);
        }
        return map;
    }
}

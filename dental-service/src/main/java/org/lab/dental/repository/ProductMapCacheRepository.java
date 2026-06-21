package org.lab.dental.repository;

import org.lab.model.ProductMap;
import org.lab.model.ProductType;
import java.util.Optional;
import java.util.UUID;

public interface ProductMapCacheRepository {

    void save(ProductMap map);

    Optional<ProductMap> save(ProductType productType, UUID userId);

    Optional<ProductMap> get(UUID userId);

    Optional<ProductMap> updateIfContains(ProductType productType, UUID userId);

    void delete(UUID id, UUID userId);
}

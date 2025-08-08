package org.lab.uimvc.datasource.redis;

import org.lab.model.ProductMap;
import java.util.Optional;
import java.util.UUID;

public interface ProductMapRepository {

    void save(ProductMap map);

    Optional<ProductMap> get(UUID userId);
}

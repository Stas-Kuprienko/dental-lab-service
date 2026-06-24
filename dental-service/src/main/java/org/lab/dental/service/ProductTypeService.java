package org.lab.dental.service;

import org.lab.model.ProductMap;
import org.lab.model.ProductType;
import org.lab.request.NewProductType;
import java.util.UUID;

public interface ProductTypeService {

    ProductMap create(NewProductType newProductType, UUID userId);

    ProductType getByIdAndUserId(UUID id, UUID userId);

    ProductMap getAllByUserId(UUID userId);

    void update(UUID id, UUID userId, float newPrice);

    void delete(UUID id, UUID userId);
}

package org.lab.telegram_bot.service;

import org.dental.restclient.ProductMapService;
import org.lab.model.ProductMap;
import org.lab.model.ProductType;
import org.lab.request.NewProductType;
import org.springframework.http.HttpHeaders;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class ProductMapServiceWrapper {

    private final ProductMapService productMapService;
    private final Function<UUID, Consumer<HttpHeaders>> httpHeaderConsumerFunction;

    ProductMapServiceWrapper(ProductMapService productMapService, Function<UUID, Consumer<HttpHeaders>> httpHeaderConsumerFunction) {
        this.productMapService = productMapService;
        this.httpHeaderConsumerFunction = httpHeaderConsumerFunction;
    }


    public ProductType create(NewProductType newProductType, UUID userId) {
        return productMapService.create(newProductType, httpHeaderConsumerFunction.apply(userId));
    }

    public ProductMap findAll(UUID userId) {
        return productMapService.findAll(httpHeaderConsumerFunction.apply(userId));
    }

    public void updatePrice(UUID id, float newPrice, UUID userId) {
        productMapService.updateProductType(id, newPrice, httpHeaderConsumerFunction.apply(userId));
    }

    public void delete(UUID id, UUID userId) {
        productMapService.delete(id, httpHeaderConsumerFunction.apply(userId));
    }
}

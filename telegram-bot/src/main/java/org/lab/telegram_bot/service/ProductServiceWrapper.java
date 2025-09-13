package org.lab.telegram_bot.service;

import org.dental.restclient.ProductService;
import org.lab.model.DentalWork;
import org.lab.request.NewProduct;
import org.springframework.http.HttpHeaders;
import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class ProductServiceWrapper {

    private final ProductService productService;
    private final Function<UUID, Consumer<HttpHeaders>> httpHeaderConsumerFunction;

    public ProductServiceWrapper(ProductService productService, Function<UUID, Consumer<HttpHeaders>> httpHeaderConsumerFunction) {
        this.productService = productService;
        this.httpHeaderConsumerFunction = httpHeaderConsumerFunction;
    }


    public DentalWork addProduct(Long workId, NewProduct newProduct, UUID userId) {
        return productService.addProduct(workId, newProduct, httpHeaderConsumerFunction.apply(userId));
    }

    public DentalWork updateCompletion(Long workId, UUID productId, LocalDate completeAt, UUID userId) {
        return productService.updateCompletion(workId, productId, completeAt, httpHeaderConsumerFunction.apply(userId));
    }

    public DentalWork deleteProduct(Long workId, UUID productId, UUID userId) {
        return productService.deleteProduct(workId, productId, httpHeaderConsumerFunction.apply(userId));
    }
}

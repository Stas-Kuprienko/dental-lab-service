package org.dental.restclient;

import org.lab.model.DentalWork;
import org.lab.request.NewProduct;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Consumer;

public class ProductService {

    private static final String RESOURCE = "/dental_works/%d/products";

    private final RestClient restClient;


    ProductService(RestClient restClient) {
        this.restClient = restClient;
    }


    public DentalWork addProduct(Long workId, NewProduct newProduct) {
        return restClient
                .post()
                .uri(RESOURCE.formatted(workId))
                .body(newProduct)
                .retrieve()
                .body(DentalWork.class);
    }

    public DentalWork addProduct(Long workId, NewProduct newProduct, Consumer<HttpHeaders> headersConsumer) {
        return restClient
                .post()
                .uri(RESOURCE.formatted(workId))
                .headers(headersConsumer)
                .body(newProduct)
                .retrieve()
                .body(DentalWork.class);
    }

    public DentalWork updateCompletion(Long workId, UUID productId, LocalDate completeAt) {
        return restClient
                .put()
                .uri(RESOURCE.formatted(workId) + '/' + productId)
                .body(completeAt)
                .retrieve()
                .body(DentalWork.class);
    }

    public DentalWork updateCompletion(Long workId, UUID productId, LocalDate completeAt, Consumer<HttpHeaders> headersConsumer) {
        return restClient
                .put()
                .uri(RESOURCE.formatted(workId) + '/' + productId)
                .headers(headersConsumer)
                .body(completeAt)
                .retrieve()
                .body(DentalWork.class);
    }

    public DentalWork deleteProduct(Long workId, UUID productId) {
        return restClient
                .delete()
                .uri(RESOURCE.formatted(workId) + '/' + productId)
                .retrieve()
                .body(DentalWork.class);
    }

    public DentalWork deleteProduct(Long workId, UUID productId, Consumer<HttpHeaders> headersConsumer) {
        return restClient
                .delete()
                .uri(RESOURCE.formatted(workId) + '/' + productId)
                .headers(headersConsumer)
                .retrieve()
                .body(DentalWork.class);
    }
}

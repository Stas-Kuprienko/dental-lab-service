package org.dental.restclient;

import org.lab.model.DentalWork;
import org.lab.request.NewProduct;
import org.springframework.web.client.RestClient;
import java.time.LocalDate;
import java.util.UUID;

public class ProductService {

    private static final String RESOURCE = "/dental_works";
    private static final String URI = "/%d/products";

    private final RestClient restClient;


    ProductService(String baseUrl, RestClient.Builder restClientBuilder) {
        baseUrl += RESOURCE;
        restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }


    public DentalWork addProduct(Long workId, NewProduct newProduct) {
        return restClient
                .post()
                .uri(URI.formatted(workId))
                .body(newProduct)
                .retrieve()
                .body(DentalWork.class);
    }

    public DentalWork updateCompletion(Long workId, UUID productId, LocalDate completeAt) {
        return restClient
                .put()
                .uri(URI.formatted(workId) + '/' + productId)
                .body(completeAt)
                .retrieve()
                .body(DentalWork.class);
    }

    public DentalWork deleteProduct(Long workId, UUID productId) {
        return restClient
                .delete()
                .uri(URI.formatted(workId) + '/' + productId)
                .retrieve()
                .body(DentalWork.class);
    }
}

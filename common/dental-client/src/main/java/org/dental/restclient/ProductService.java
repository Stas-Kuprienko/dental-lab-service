package org.dental.restclient;

import org.lab.model.DentalWork;
import org.lab.request.NewProduct;
import org.springframework.web.client.RestClient;
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


    public DentalWork addProduct(UUID userId, Long workId, NewProduct newProduct) {
        return restClient
                .post()
                .uri(URI.formatted(workId))
                .header(DentalLabRestClient.HEADER, userId.toString())
                .body(newProduct)
                .retrieve()
                .body(DentalWork.class);
    }

    public DentalWork deleteProduct(UUID userId, Long workId, UUID productId) {
        return restClient
                .delete()
                .uri(URI.formatted(workId) + '/' + productId)
                .header(DentalLabRestClient.HEADER, userId.toString())
                .retrieve()
                .body(DentalWork.class);
    }
}

package org.dental.restclient;

import org.lab.model.ProductMap;
import org.lab.model.ProductType;
import org.lab.request.NewProductType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import java.util.Optional;
import java.util.UUID;

public class ProductMapService {

    private static final String RESOURCE = "/product_map";

    private final RestClient restClient;


    ProductMapService(String baseUrl, RestClient.Builder restClientBuilder) {
        baseUrl += RESOURCE;
        restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }


    public ProductType create(NewProductType newProductType) {
        return restClient
                .post()
                .body(newProductType)
                .retrieve()
                .body(ProductType.class);
    }

    public Optional<ProductType> findById(UUID id) {
        ResponseEntity<ProductType> response = restClient
                .get()
                .uri(DentalLabRestClient.uriById(id))
                .retrieve()
                .toEntity(ProductType.class);
        if (response.getStatusCode().value() == 200) {
            return Optional.of(response.getBody());
        } else if (response.getStatusCode().value() == 404) {
            return Optional.empty();
        } else {
            //TODO
            throw new RuntimeException();
        }
    }

    public ProductMap findAll() {
        return restClient
                .get()
                .retrieve()
                .body(ProductMap.class);
    }

    public void updateProductType(UUID id, float newPrice) {
        ResponseEntity<Void> response = restClient
                .put()
                .uri(DentalLabRestClient.uriById(id))
                .body(newPrice)
                .retrieve()
                .toBodilessEntity();
        if (response.getStatusCode().value() != 200) {
            //TODO
            throw new RuntimeException(response.toString());
        }
    }

    public void delete(UUID id) {
        restClient
                .delete()
                .uri(DentalLabRestClient.uriById(id))
                .retrieve()
                .toBodilessEntity();
    }
}

package org.dental.restclient;

import org.springframework.web.client.RestClient;

public class DentalLabRestClient {

    //TEMPORARY!!!
    public static final String HEADER = "X-USER-ID";

    public final ProductMapService PRODUCT_MAP;
    public final DentalWorkService DENTAL_WORKS;
    public final ProductService PRODUCTS;


    public DentalLabRestClient(String baseUrl, RestClient.Builder restClientBuilder) {
        PRODUCT_MAP = new ProductMapService(baseUrl, restClientBuilder);
        DENTAL_WORKS = new DentalWorkService(baseUrl, restClientBuilder);
        PRODUCTS = new ProductService(baseUrl, restClientBuilder);
    }


    public static String uriById(Object id) {
        return "/%s".formatted(id.toString());
    }
}

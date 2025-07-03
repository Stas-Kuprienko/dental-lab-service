package org.dental.restclient;

import org.springframework.web.client.RestClient;

public class DentalLabRestClient {

    //TEMPORARY!!!
    public static final String HEADER = "X-USER-ID";

    public final ProductMapService PRODUCT_MAP;


    public DentalLabRestClient(String baseUrl, RestClient.Builder restClientBuilder) {
        PRODUCT_MAP = new ProductMapService(baseUrl, restClientBuilder);
    }


    public static String uriById(Object id) {
        return "/%s".formatted(id.toString());
    }
}

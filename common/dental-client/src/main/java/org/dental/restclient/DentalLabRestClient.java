package org.dental.restclient;

import org.springframework.web.client.RestClient;

public class DentalLabRestClient {

    public final AuthenticationService AUTHENTICATION;
    public final UserService USERS;
    public final TelegramChatService TELEGRAM_CHATS;
    public final ProductMapService PRODUCT_MAP;
    public final DentalWorkService DENTAL_WORKS;
    public final ProductService PRODUCTS;
    public final ReportService REPORTS;
    public final WorkPhotoLinkService PHOTO_LINKS;


    public DentalLabRestClient(String baseUrl, RestClient.Builder restClientBuilder) {
        AUTHENTICATION = new AuthenticationService(baseUrl, restClientBuilder);
        USERS = new UserService(baseUrl, restClientBuilder);
        TELEGRAM_CHATS = new TelegramChatService(baseUrl, restClientBuilder);
        PRODUCT_MAP = new ProductMapService(baseUrl, restClientBuilder);
        DENTAL_WORKS = new DentalWorkService(baseUrl, restClientBuilder);
        PRODUCTS = new ProductService(baseUrl, restClientBuilder);
        REPORTS = new ReportService(baseUrl, restClientBuilder);
        PHOTO_LINKS = new WorkPhotoLinkService(baseUrl, restClientBuilder);
    }


    static String uriById(Object id) {
        return "/%s".formatted(id.toString());
    }
}

package org.dental.restclient;

import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

public class DentalLabRestClient {

    public final AuthenticationService AUTHENTICATION;
    public final UserService USERS;
    public final CredentialService CREDENTIALS;
    public final VerificationService VERIFICATION;
    public final TelegramChatService TELEGRAM_CHATS;
    public final ProductMapService PRODUCT_MAP;
    public final DentalWorkService DENTAL_WORKS;
    public final ProductService PRODUCTS;
    public final ReportService REPORTS;
    public final WorkPhotoLinkService PHOTO_LINKS;


    public DentalLabRestClient(String baseUrl, RestClient.Builder restClientBuilder, ClientHttpRequestInterceptor interceptor) {
        restClientBuilder.baseUrl(baseUrl);
        AUTHENTICATION = new AuthenticationService(restClientBuilder.build());
        RestClient restClient = restClientBuilder.requestInterceptor(interceptor).build();
        CREDENTIALS = new CredentialService(restClient);
        USERS = new UserService(restClient);
        VERIFICATION = new VerificationService(restClient);
        TELEGRAM_CHATS = new TelegramChatService(restClient);
        PRODUCT_MAP = new ProductMapService(restClient);
        DENTAL_WORKS = new DentalWorkService(restClient);
        PRODUCTS = new ProductService(restClient);
        REPORTS = new ReportService(restClient);
        PHOTO_LINKS = new WorkPhotoLinkService(restClient);
    }


    static String uriById(Object id) {
        return "/%s".formatted(id.toString());
    }
}

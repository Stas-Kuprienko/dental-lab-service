package org.lab.telegram_bot.service;

import org.dental.restclient.DentalLabRestClient;
import org.lab.telegram_bot.configuration.TelegramBotConfig;
import org.lab.telegram_bot.configuration.auth.AuthenticationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import java.util.UUID;
import java.util.function.Consumer;

@Component
public class DentalLabRestClientWrapper {

    private static final String USER_ID_HEADER = "X-USER-ID";
    private static final String SERVICE_ID_HEADER = "X-SERVICE-ID";

    private final AuthenticationManager authenticationManager;
    public final TelegramChatServiceWrapper TELEGRAM_CHATS;
    public final ProductMapServiceWrapper PRODUCT_MAP;
    public final DentalWorkServiceWrapper DENTAL_WORKS;
    public final ProductServiceWrapper PRODUCTS;
    public final ReportServiceWrapper REPORTS;


    @Autowired
    public DentalLabRestClientWrapper(AuthenticationManager authenticationManager,
                                      DentalLabRestClient dentalLabRestClient) {
        this.authenticationManager = authenticationManager;
        TELEGRAM_CHATS = new TelegramChatServiceWrapper(dentalLabRestClient.TELEGRAM_CHATS, httpHeadersConsumer());
        PRODUCT_MAP = new ProductMapServiceWrapper(dentalLabRestClient.PRODUCT_MAP, this::httpHeadersConsumer);
        DENTAL_WORKS = new DentalWorkServiceWrapper(dentalLabRestClient.DENTAL_WORKS, this::httpHeadersConsumer);
        PRODUCTS = new ProductServiceWrapper(dentalLabRestClient.PRODUCTS, this::httpHeadersConsumer);
        REPORTS = new ReportServiceWrapper(dentalLabRestClient.REPORTS, this::httpHeadersConsumer);
    }


    public Consumer<HttpHeaders> httpHeadersConsumer(UUID userId) {
        return httpHeaders -> {
            httpHeaders.setBearerAuth(authenticationManager.accessToken());
            httpHeaders.set(USER_ID_HEADER, userId.toString());
        };
    }

    public Consumer<HttpHeaders> httpHeadersConsumer() {
        return httpHeaders -> {
            httpHeaders.setBearerAuth(authenticationManager.accessToken());
            httpHeaders.set(SERVICE_ID_HEADER, TelegramBotConfig.SERVICE_CLIENT_ID);
        };
    }
}

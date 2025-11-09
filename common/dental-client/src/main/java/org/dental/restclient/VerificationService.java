package org.dental.restclient;

import org.springframework.web.client.RestClient;

public class VerificationService {

    private static final String RESOURCE = "/verification";

    private final RestClient restClient;


    VerificationService(String baseUrl, RestClient.Builder restClientBuilder) {
        baseUrl += RESOURCE;
        restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }


    public void sendVerificationLink(String email, boolean toChange) {
        restClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/email")
                        .queryParam("to-change", toChange)
                        .build())
                .body(email)
                .retrieve()
                .toBodilessEntity();
    }

    public void sendTelegramOtp(String email) {
        restClient
                .post()
                .uri("/telegram-otp")
                .body(email)
                .retrieve()
                .toBodilessEntity();
    }

    public boolean verifyUserEmail(String token, boolean toChange) {
        return restClient
                .put()
                .uri(uriBuilder -> uriBuilder
                        .path("/email" + DentalLabRestClient.uriById(token))
                        .queryParam("to-change", toChange)
                        .build())
                .retrieve()
                .body(Boolean.class);
    }

    public boolean isVerified(String email) {
        return restClient
                .post()
                .uri("/email" + DentalLabRestClient.uriById(email))
                .retrieve()
                .body(Boolean.class);
    }
}

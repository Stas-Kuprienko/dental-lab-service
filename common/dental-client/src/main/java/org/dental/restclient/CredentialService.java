package org.dental.restclient;

import org.lab.request.ResetPasswordRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import java.util.function.Consumer;

public class CredentialService {

    private static final String RESOURCE = "/credentials";
    private static final String RESET_PASSWORD_URI = "/reset-password";

    private final RestClient restClient;


    CredentialService(RestClient restClient) {
        this.restClient = restClient;
    }


    public void sendResetPasswordLink(String email) {
        restClient
                .post()
                .uri(RESOURCE + RESET_PASSWORD_URI)
                .body(email)
                .retrieve()
                .toBodilessEntity();
    }

    public void sendResetPasswordLink(String email, Consumer<HttpHeaders> headersConsumer) {
        restClient
                .post()
                .uri(RESOURCE + RESET_PASSWORD_URI)
                .headers(headersConsumer)
                .body(email)
                .retrieve()
                .toBodilessEntity();
    }

    public boolean verifyResetPasswordToken(String token, String email) {
        return restClient
                .patch()
                .uri(RESOURCE + RESET_PASSWORD_URI + DentalLabRestClient.uriById(token))
                .body(email)
                .retrieve()
                .body(Boolean.class);
    }

    public boolean verifyResetPasswordToken(String token, String email, Consumer<HttpHeaders> headersConsumer) {
        return restClient
                .patch()
                .uri(RESOURCE + RESET_PASSWORD_URI + DentalLabRestClient.uriById(token))
                .headers(headersConsumer)
                .body(email)
                .retrieve()
                .body(Boolean.class);
    }

    public void resetPassword(ResetPasswordRequest request) {
        restClient
                .put()
                .uri(RESOURCE + RESET_PASSWORD_URI)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    public void resetPassword(ResetPasswordRequest request, Consumer<HttpHeaders> headersConsumer) {
        restClient
                .put()
                .uri(RESOURCE + RESET_PASSWORD_URI)
                .headers(headersConsumer)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    public void deleteResetPasswordToken(String email) {
        restClient
                .delete()
                .uri(uriBuilder -> uriBuilder
                        .path(RESOURCE + RESET_PASSWORD_URI)
                        .queryParam("email", email)
                        .build())
                .retrieve()
                .toBodilessEntity();
    }

    public void deleteResetPasswordToken(String email, Consumer<HttpHeaders> headersConsumer) {
        restClient
                .delete()
                .uri(uriBuilder -> uriBuilder
                        .path(RESOURCE + RESET_PASSWORD_URI)
                        .queryParam("email", email)
                        .build())
                .headers(headersConsumer)
                .retrieve()
                .toBodilessEntity();
    }
}

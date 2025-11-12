package org.dental.restclient;

import org.lab.model.AuthToken;
import org.lab.model.LoginCredential;
import org.lab.request.ClientCredentialsRequest;
import org.lab.request.LoginRequest;
import org.lab.request.RefreshTokenRequest;
import org.lab.request.ResetPasswordRequest;
import org.springframework.web.client.RestClient;
import java.util.function.BiFunction;

public class AuthenticationService extends ClientExceptionDispatcher {

    private static final String RESOURCE = "/auth";
    private static final String RESET_PASSWORD_URI = "/reset-password";

    private final RestClient restClient;


    AuthenticationService(String baseUrl, RestClient.Builder restClientBuilder) {
        baseUrl += RESOURCE;
        restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }


    public AuthToken clientLogin(String clientId, String clientSecret) {
        return restClient
                .post()
                .uri("/login/client-id")
                .body(new ClientCredentialsRequest(clientId, clientSecret))
                .retrieve()
                .body(AuthToken.class);
    }

    public LoginCredential userLogin(String email, String password) {
        return restClient
                .post()
                .uri("/login")
                .body(new LoginRequest(email, password))
                .retrieve()
                .body(LoginCredential.class);
    }

    public AuthToken refresh(String refreshToken) {
        return restClient
                .post()
                .uri("/refresh")
                .body(new RefreshTokenRequest(refreshToken))
                .retrieve()
                .body(AuthToken.class);
    }

    public void sendResetPasswordLink(String email) {
        restClient
                .post()
                .uri(RESET_PASSWORD_URI)
                .body(email)
                .retrieve()
                .toBodilessEntity();
    }

    public boolean verifyResetPasswordToken(String token, String email) {
        return restClient
                .patch()
                .uri(RESET_PASSWORD_URI + DentalLabRestClient.uriById(token))
                .body(email)
                .retrieve()
                .body(Boolean.class);
    }

    public void resetPassword(ResetPasswordRequest request) {
        restClient
                .put()
                .uri(RESET_PASSWORD_URI)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    public void deleteResetPasswordToken(String email) {
        restClient
                .delete()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("email", email)
                        .build())
                .retrieve()
                .toBodilessEntity();
    }

    public BiFunction<String, String, AuthToken> getClientAuthenticationFunction() {
        return this::clientLogin;
    }
}

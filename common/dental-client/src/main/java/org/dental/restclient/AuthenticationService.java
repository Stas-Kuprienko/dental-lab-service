package org.dental.restclient;

import org.lab.model.AuthToken;
import org.lab.model.LoginCredential;
import org.lab.request.ClientCredentialsRequest;
import org.lab.request.LoginRequest;
import org.lab.request.RefreshTokenRequest;
import org.lab.request.ResetPasswordRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

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
        ResponseEntity<AuthToken> response = restClient
                .post()
                .uri("/login/client-id")
                .body(new ClientCredentialsRequest(clientId, clientSecret))
                .retrieve()
                .toEntity(AuthToken.class);
        check(response);
        return getBodyOrThrowNotFoundEx(response, "authentication error");
    }

    public LoginCredential userLogin(String email, String password) {
        ResponseEntity<LoginCredential> response = restClient
                .post()
                .uri("/login")
                .body(new LoginRequest(email, password))
                .retrieve()
                .toEntity(LoginCredential.class);
        check(response);
        return getBodyOrThrowNotFoundEx(response, "authentication error");
    }

    public AuthToken refresh(String refreshToken) {
        ResponseEntity<AuthToken> response = restClient
                .post()
                .uri("/refresh")
                .body(new RefreshTokenRequest(refreshToken))
                .retrieve()
                .toEntity(AuthToken.class);
        check(response);
        return getBodyOrThrowNotFoundEx(response, "refresh token error");
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
}

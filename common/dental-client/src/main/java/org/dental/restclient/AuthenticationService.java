package org.dental.restclient;

import org.lab.model.AuthToken;
import org.lab.model.LoginCredential;
import org.lab.request.LoginRequest;
import org.lab.request.RefreshTokenRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

public class AuthenticationService extends ClientExceptionDispatcher {

    private static final String RESOURCE = "/auth";

    private final RestClient restClient;


    AuthenticationService(String baseUrl, RestClient.Builder restClientBuilder) {
        baseUrl += RESOURCE;
        restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }


    public LoginCredential login(String email, String password) {
        ResponseEntity<LoginCredential> response = restClient
                .post()
                .uri("/login")
                .body(new LoginRequest(email, password))
                .retrieve()
                .toEntity(LoginCredential.class);
        check(response);
        //TODO
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
        //TODO
        return getBodyOrThrowNotFoundEx(response, "refresh token error");
    }
}

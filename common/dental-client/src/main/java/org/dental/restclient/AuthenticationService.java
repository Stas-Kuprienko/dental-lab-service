package org.dental.restclient;

import org.lab.model.AuthToken;
import org.lab.model.LoginCredential;
import org.lab.request.ClientCredentialsRequest;
import org.lab.request.LoginRequest;
import org.lab.request.RefreshTokenRequest;
import org.springframework.web.client.RestClient;

public class AuthenticationService extends ClientExceptionDispatcher {

    private static final String RESOURCE = "/auth";

    private final RestClient restClient;


    AuthenticationService(RestClient restClient) {
        this.restClient = restClient;
    }


    public AuthToken clientLogin(String clientId, String clientSecret) {
        return restClient
                .post()
                .uri(RESOURCE + "/login/client-id")
                .body(new ClientCredentialsRequest(clientId, clientSecret))
                .retrieve()
                .body(AuthToken.class);
    }

    public LoginCredential userLogin(String email, String password) {
        return restClient
                .post()
                .uri(RESOURCE + "/login")
                .body(new LoginRequest(email, password))
                .retrieve()
                .body(LoginCredential.class);
    }

    public AuthToken refresh(String refreshToken) {
        return restClient
                .post()
                .uri(RESOURCE + "/refresh")
                .body(new RefreshTokenRequest(refreshToken))
                .retrieve()
                .body(AuthToken.class);
    }
}

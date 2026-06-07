package org.lab.telegram_bot.configuration.auth;

import lombok.extern.slf4j.Slf4j;
import org.lab.model.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
public class ServiceAuthenticationManager {

    private static final String TOKEN_URI = "/protocol/openid-connect/token";

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;
    private TokenTracker tokenTracker;


    @Autowired
    public ServiceAuthenticationManager(RestClient.Builder restClientBuilder,
                                        @Value("${project.variables.keycloak.url}") String keycloakUrl,
                                        @Value("${project.variables.keycloak.realm}") String keycloakRealm,
                                        @Value("${project.variables.keycloak.client-id}") String clientId,
                                        @Value("${project.variables.keycloak.client-secret}") String clientSecret) {
        this.restClient = restClientBuilder
                .baseUrl(keycloakUrl + "/realms/" + keycloakRealm)
                .build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }


    public void authenticate() {
        login();
        log.info("Client '{}' authentication is successful", clientId);
    }

    public String accessToken() {
        if (tokenTracker == null) {
            login();
        } else if (tokenTracker.isExpired()) {
            if (tokenTracker.isRefreshExpired()) {
                login();
            } else {
                refreshToken();
            }
        }
        return tokenTracker.accessToken();
    }


    private void login() {
        AuthToken token = clientLogin(clientId, clientSecret);
        this.tokenTracker = new TokenTracker(token);
    }

    private void refreshToken() {
        if (tokenTracker == null) {
            login();
        } else {
            AuthToken token = refresh(tokenTracker.refreshToken());
            this.tokenTracker = new TokenTracker(token);
        }
    }

    private AuthToken clientLogin(String clientId, String clientSecret) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "client_credentials");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        AuthToken token = requestToken(params);
        log.info("Keycloak client '{}' is logged in.", clientId);
        return token;
    }

    private AuthToken refresh(String refreshToken) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "refresh_token");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("refresh_token", refreshToken);
        AuthToken token = requestToken(params);
        log.info("Keycloak client '{}' is refreshed token.", clientId);
        return token;
    }

    private AuthToken requestToken(MultiValueMap<String, String> params) {
        return restClient.post()
                .uri(TOKEN_URI)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(params)
                .retrieve()
                .body(AuthToken.class);
    }


    static class TokenTracker {

        private final AuthToken token;
        private final Instant expiresIn;
        private final Instant refreshExpiresIn;

        TokenTracker(AuthToken token) {
            this.token = token;
            expiresIn = Instant.now().plus(token.getExpiresIn(), ChronoUnit.SECONDS);
            refreshExpiresIn = Instant.now().plus(token.getRefreshExpiresIn(), ChronoUnit.SECONDS);
        }


        String accessToken() {
            return token.getAccessToken();
        }

        String refreshToken() {
            return token.getRefreshToken();
        }

        boolean isExpired() {
            return expiresIn.isBefore(Instant.now());
        }

        boolean isRefreshExpired() {
            return refreshExpiresIn.isBefore(Instant.now());
        }
    }
}

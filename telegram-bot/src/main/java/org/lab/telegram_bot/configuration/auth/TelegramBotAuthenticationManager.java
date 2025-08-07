package org.lab.telegram_bot.configuration.auth;

import jakarta.annotation.PostConstruct;
import org.dental.restclient.AuthenticationService;
import org.dental.restclient.DentalLabRestClient;
import org.lab.model.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class TelegramBotAuthenticationManager {

    private final AuthenticationService authenticationService;
    private final String clientId;
    private final String clientSecret;
    private TokenTracker tokenTracker;


    @Autowired
    public TelegramBotAuthenticationManager(DentalLabRestClient dentalLabRestClient,
                                            @Value("${project.variables.keycloak.client-id}") String clientId,
                                            @Value("${project.variables.keycloak.client-secret}") String clientSecret) {
        this.authenticationService = dentalLabRestClient.AUTHENTICATION;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }


    @PostConstruct
    public void authenticate() {
        login();
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
        AuthToken token = authenticationService.clientLogin(clientId, clientSecret);
        this.tokenTracker = new TokenTracker(token);
    }

    private void refreshToken() {
        if (tokenTracker == null) {
            login();
        } else {
            AuthToken token = authenticationService.refresh(tokenTracker.refreshToken());
            this.tokenTracker = new TokenTracker(token);
        }
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

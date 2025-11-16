package org.lab.uimvc.configuration.auth;

import lombok.extern.slf4j.Slf4j;
import org.dental.restclient.AuthenticationService;
import org.lab.model.AuthToken;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
public class ClientAuthenticationManager {

    private final AuthenticationService authenticationService;
    private final String clientId;
    private final String clientSecret;
    private TokenTracker tokenTracker;

    public ClientAuthenticationManager(AuthenticationService authenticationService, String clientId, String clientSecret) {
        this.authenticationService = authenticationService;
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

package org.lab.uimvc.configuration.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class TokenRequestInterceptor implements ClientHttpRequestInterceptor {

    private final OAuth2AuthorizedClientManager authorizedClientManager;

    @Autowired
    public TokenRequestInterceptor(OAuth2AuthorizedClientManager authorizedClientManager) {
        this.authorizedClientManager = authorizedClientManager;
    }


    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId("keycloak")
                .principal(authentication)
                .build();
        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);
        if (authorizedClient != null) {
            String token = authorizedClient.getAccessToken().getTokenValue();
            request.getHeaders().setBearerAuth(token);
        }
        return execution.execute(request, body);
    }
}

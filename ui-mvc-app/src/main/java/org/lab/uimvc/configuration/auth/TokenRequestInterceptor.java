package org.lab.uimvc.configuration.auth;

import org.lab.exception.ApplicationCustomException;
import org.lab.model.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.function.BiFunction;

@Component
public class TokenRequestInterceptor implements ClientHttpRequestInterceptor {

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final String clientId;
    private final String clientSecret;
    private BiFunction<String, String, AuthToken> authentication;

    @Autowired
    public TokenRequestInterceptor(OAuth2AuthorizedClientManager authorizedClientManager,
                                   @Value("${project.variables.keycloak.service-client-id}") String clientId,
                                   @Value("${project.variables.keycloak.service-client-secret}") String clientSecret) {
        this.authorizedClientManager = authorizedClientManager;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }


    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (isSecured(request)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId("keycloak")
                    .principal(authentication)
                    .build();
            OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);
            if (authorizedClient != null) {
                String token = authorizedClient.getAccessToken().getTokenValue();
                request.getHeaders().setBearerAuth(token);
            } else {
                throw new AccessDeniedException(
                        "Unauthorized user trying to make request for secured endpoint: " + request.getURI().getPath());
            }
        } else {
            if (authentication == null) {
                throw new ApplicationCustomException("Authentication function is not set for Request Interceptor!");
            }
            AuthToken token = authentication.apply(clientId, clientSecret);
            request.getHeaders().setBearerAuth(token.getAccessToken());
        }
        return execution.execute(request, body);
    }

    public void setAuthentication(BiFunction<String, String, AuthToken> authentication) {
        this.authentication = authentication;
    }


    private boolean isSecured(HttpRequest request) {
        return !(
                (request.getURI().getPath().equals("/api/v1/users") && request.getMethod().equals(HttpMethod.POST))
                ||
                request.getURI().getPath().startsWith("/api/v1/auth/")
        );
    }
}

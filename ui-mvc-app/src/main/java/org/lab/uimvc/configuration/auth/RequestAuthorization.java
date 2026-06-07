package org.lab.uimvc.configuration.auth;

import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;

@Component
public class RequestAuthorization {

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final ServiceAuthenticationManager authenticationManager;

    @Autowired
    public RequestAuthorization(OAuth2AuthorizedClientManager authorizedClientManager,
                                ServiceAuthenticationManager authenticationManager) {
        this.authorizedClientManager = authorizedClientManager;
        this.authenticationManager = authenticationManager;
        authenticationManager.authenticate();
    }


    public void intercept(RequestTemplate request) {
        if (isSecured(request)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId("keycloak")
                    .principal(authentication)
                    .build();
            OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);
            if (authorizedClient != null) {
                String token = authorizedClient.getAccessToken().getTokenValue();
                request.header("Authorization", "Bearer " + token);
            } else {
                throw new AccessDeniedException(
                        "Unauthorized user trying to make request for secured endpoint: " + request.path());
            }
        } else {
            request.header("Authorization", "Bearer " + authenticationManager.accessToken());
        }
    }


    private boolean isSecured(RequestTemplate request) {
        String path = request.path();
        HttpMethod method = HttpMethod.valueOf(request.method());
        return !(
                (path.equals("/api/v1/users") && method.equals(HttpMethod.POST))
                ||
                (path.startsWith("/api/v1/auth"))
                ||
                (path.startsWith("/api/v1/credentials/")));
    }
}

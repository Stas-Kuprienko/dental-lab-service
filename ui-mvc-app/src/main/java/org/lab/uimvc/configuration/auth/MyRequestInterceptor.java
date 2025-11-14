package org.lab.uimvc.configuration.auth;

import org.lab.exception.ApplicationCustomException;
import org.springframework.beans.factory.annotation.Autowired;
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

@Component
public class MyRequestInterceptor implements ClientHttpRequestInterceptor {

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private ClientAuthenticationManager authenticationManager;

    @Autowired
    public MyRequestInterceptor(OAuth2AuthorizedClientManager authorizedClientManager) {
        this.authorizedClientManager = authorizedClientManager;
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
            if (authenticationManager == null) {
                throw new ApplicationCustomException("AuthenticationManager is not set for Request Interceptor!");
            }
            request.getHeaders().setBearerAuth(authenticationManager.accessToken());
        }
        return execution.execute(request, body);
    }

    public void setAuthentication(ClientAuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }


    private boolean isSecured(HttpRequest request) {
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();
        return !(
                (path.equals("/api/v1/users") && method.equals(HttpMethod.POST))
                ||
                (path.startsWith("/api/v1/credentials/")));
    }
}

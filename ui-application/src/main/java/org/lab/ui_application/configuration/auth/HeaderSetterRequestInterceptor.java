package org.lab.ui_application.configuration.auth;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import java.io.IOException;

public class HeaderSetterRequestInterceptor implements ClientHttpRequestInterceptor {

    private final HttpSession session;
    private final String backendBaseUrl;

    @Autowired
    public HeaderSetterRequestInterceptor(HttpSession session, String backendBaseUrl) {
        this.session = session;
        this.backendBaseUrl = backendBaseUrl;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String requestUri = request.getURI().toString();

        if (requestUri.startsWith(backendBaseUrl)) {
            String accessToken = (String) session.getAttribute("ACCESS_TOKEN");

            if (accessToken != null) {
                request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
            }
        }

        return execution.execute(request, body);
    }
}

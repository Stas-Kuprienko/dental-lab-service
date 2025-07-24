package org.lab.telegram_bot.configuration.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class TokenRequestInterceptor implements ClientHttpRequestInterceptor {

    private final TelegramBotAuthenticationManager authenticationManager;

    @Autowired
    public TokenRequestInterceptor(TelegramBotAuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }


    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String token = authenticationManager.getToken(null);
        request.getHeaders().setBearerAuth(token);
        return execution.execute(request, body);
    }
}

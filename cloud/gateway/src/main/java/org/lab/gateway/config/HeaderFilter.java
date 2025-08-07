package org.lab.gateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class HeaderFilter implements GatewayFilter {

    private static final String TELEGRAM_CLIENT_ID = "telegram-bot-client";
    private static final String USER_ID_HEADER = "X-USER-ID";
    private static final String CLIENT_ID_CLAIM = "azp";


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .filter(principal -> principal instanceof JwtAuthenticationToken)
                .map(principal -> (JwtAuthenticationToken) principal)
                .map(jwtAuth -> setUserIdHeader(exchange, jwtAuth))
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter);
    }


    private ServerWebExchange setUserIdHeader(ServerWebExchange exchange, JwtAuthenticationToken jwtAuth) {
        String clientId = jwtAuth.getToken().getClaimAsString(CLIENT_ID_CLAIM);
        if (TELEGRAM_CLIENT_ID.equals(clientId)) {
            if (!exchange.getRequest().getHeaders().containsKey(USER_ID_HEADER)) {
                throw new IllegalStateException("Missing X-USER-ID header for telegram client");
            } else {
                return exchange;
            }
        } else {
            String userId = jwtAuth.getToken().getSubject();
            ServerHttpRequest mutatedRequest = exchange
                    .getRequest()
                    .mutate()
                    .header(USER_ID_HEADER, userId)
                    .build();
            return exchange.mutate().request(mutatedRequest).build();
        }
    }
}
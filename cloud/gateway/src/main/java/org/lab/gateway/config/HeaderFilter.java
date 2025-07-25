package org.lab.gateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
public class HeaderFilter extends AbstractGatewayFilterFactory<Object> {


    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) ->
                exchange.getPrincipal()
                        .filter(principal -> principal instanceof JwtAuthenticationToken)
                        .map(principal -> (JwtAuthenticationToken) principal)
                        .map(jwtAuth -> setUserIdHeader(exchange, jwtAuth))
                        .defaultIfEmpty(exchange)
                        .flatMap(chain::filter);
    }

    public ServerWebExchange setUserIdHeader(ServerWebExchange exchange, JwtAuthenticationToken jwtAuth) {
        String userId = jwtAuth.getToken().getSubject();
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-USER-ID", userId)
                .build();
        return exchange.mutate().request(mutatedRequest).build();
    }
}

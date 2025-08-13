package org.lab.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.lab.exception.ForbiddenCustomException;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class HttpHeaderManagementFilter implements GatewayFilter {

    private static final String TELEGRAM_CLIENT_ID = "telegram-bot-client";
    private static final String USER_ID_HEADER = "X-USER-ID";
    private static final String SERVICE_ID_HEADER = "X-SERVICE-ID";
    private static final String CLIENT_ID_CLAIM = "azp";


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .filter(principal -> principal instanceof JwtAuthenticationToken)
                .map(principal -> (JwtAuthenticationToken) principal)
                .map(jwtAuth -> manageHttpHeader(exchange, jwtAuth))
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter);
    }


    private ServerWebExchange manageHttpHeader(ServerWebExchange exchange, JwtAuthenticationToken jwtAuth) {
        String clientId = jwtAuth.getToken().getClaimAsString(CLIENT_ID_CLAIM);
        return switch (clientId) {
            case TELEGRAM_CLIENT_ID -> telegramBotClient(exchange);
            case null -> throw new ForbiddenCustomException("client ID is null, but must be set");
            default -> dentalLabClient(exchange, jwtAuth);
        };
    }


    private static ServerWebExchange dentalLabClient(ServerWebExchange exchange, JwtAuthenticationToken jwtAuth) {
        log.info("request from dental-lab-client");
        String userId = jwtAuth.getToken().getSubject();
        ServerHttpRequest mutatedRequest = exchange
                .getRequest()
                .mutate()
                .header(USER_ID_HEADER, userId)
                .build();
        return exchange.mutate().request(mutatedRequest).build();
    }

    private ServerWebExchange telegramBotClient(ServerWebExchange exchange) {
        log.info("request from telegram-bot-client");
        if (!exchange.getRequest().getHeaders().containsKey(USER_ID_HEADER)) {
            if (isTelegramClientPermitted(exchange)) {
                ServerHttpRequest mutatedRequest = exchange
                        .getRequest()
                        .mutate()
                        .header(SERVICE_ID_HEADER, TELEGRAM_CLIENT_ID)
                        .build();
                log.info("set HTTP header {}:{}", SERVICE_ID_HEADER, TELEGRAM_CLIENT_ID);
                return exchange.mutate().request(mutatedRequest).build();
            } else {
                throw new ForbiddenCustomException("Missing X-USER-ID header for telegram client");
            }
        }
        return exchange;
    }


    private boolean isTelegramClientPermitted(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getPath();
        HttpMethod method = exchange.getRequest().getMethod();
        if (path.equals("/api/v1/telegram_chat") && method.equals(HttpMethod.POST)) {
            return true;
        }
        if (path.matches("/api/v1/telegram_chat/\\d+") && method.equals(HttpMethod.GET)) {
            return true;
        }
        return false;
    }
}

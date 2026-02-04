package org.lab.dental.configuration.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.jetbrains.annotations.NotNull;
import org.lab.exception.ForbiddenCustomException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class HttpHeaderManagementFilter extends OncePerRequestFilter {

    private static final String DENTAL_LAB_CLIENT_ID = "dental-lab-client";
    private static final String UI_MVC_CLIENT_ID = "ui-mvc-client";
    private static final String TELEGRAM_CLIENT_ID = "telegram-bot-client";
    private static final String USER_ID_HEADER = "X-USER-ID";
    private static final String SERVICE_ID_HEADER = "X-SERVICE-ID";
    private static final String CLIENT_ID_CLAIM = "azp";

    private final JwtDecoder jwtDecoder;


    public HttpHeaderManagementFilter(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }


    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (token == null) {
            HttpStatus status = HttpStatus.UNAUTHORIZED;
            throw HttpClientErrorException.create(HttpStatusCode.valueOf(status.value()), status.getReasonPhrase(), null, status.getReasonPhrase().getBytes(), StandardCharsets.UTF_8);
        }
        token = token.replace("Bearer ", "");
        Jwt jwt = jwtDecoder.decode(token);
        String clientId = jwt.getClaimAsString(CLIENT_ID_CLAIM);
        log.debug("Processing request with client ID: {}", clientId);
        switch (clientId) {
            case TELEGRAM_CLIENT_ID -> handleTelegramClient(request);
            case UI_MVC_CLIENT_ID -> handleUiMvcClient(request);
            case null -> throw new ForbiddenCustomException("Client ID is null");
            default -> handleDentalLabClient(jwt, request);
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        log.info("Request URI '{}', request ID '{}'", request.getRequestURI(), request.getRequestId());
        String path = request.getRequestURI();
        return path.startsWith("/actuator/") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs/") ||
                path.startsWith("/api/v1/auth/");
    }


    private void handleDentalLabClient(Jwt jwt, HttpServletRequest request) {
        log.info("Request from {}", DENTAL_LAB_CLIENT_ID);
        String userId = jwt.getSubject();
        request.setAttribute(USER_ID_HEADER, userId);
        log.debug("Set attribute {}={}", USER_ID_HEADER, userId);
    }

    private void handleUiMvcClient(HttpServletRequest request) {
        log.info("Request from {}", UI_MVC_CLIENT_ID);
        request.setAttribute(SERVICE_ID_HEADER, UI_MVC_CLIENT_ID);
        log.debug("Set attribute {}={}", SERVICE_ID_HEADER, UI_MVC_CLIENT_ID);
    }

    private void handleTelegramClient(HttpServletRequest request) {
        log.info("Request from {}", TELEGRAM_CLIENT_ID);
        String userIdHeader = request.getHeader("X-USER-ID");
        if (userIdHeader == null) {
            if (isTelegramClientPermitted(request)) {
                request.setAttribute(SERVICE_ID_HEADER, TELEGRAM_CLIENT_ID);
                log.debug("Set attribute {}={}", SERVICE_ID_HEADER, TELEGRAM_CLIENT_ID);
            } else {
                throw new ForbiddenCustomException(
                        "Missing X-USER-ID header for telegram client");
            }
        } else {
            request.setAttribute(USER_ID_HEADER, userIdHeader);
            log.debug("Set attribute {}={} from header", USER_ID_HEADER, userIdHeader);
        }
    }

    private boolean isTelegramClientPermitted(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        log.debug("Checking permissions for path: {} method: {}", path, method);
        if (path.equals("/api/v1/telegram_chat") && method.equals(HttpMethod.POST.name())) {
            return true;
        }
        if (path.matches("/api/v1/telegram_chat/\\d+") && method.equals(HttpMethod.GET.name())) {
            return true;
        }
        if (path.startsWith("/api/v1/telegram_chat/link/") && method.equals(HttpMethod.POST.name())) {
            return true;
        }
        return false;
    }
}
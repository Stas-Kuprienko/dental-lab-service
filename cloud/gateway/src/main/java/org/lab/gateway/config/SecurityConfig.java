package org.lab.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final HeaderFilter headerFilter;
    private final String issuerUri;


    @Autowired
    public SecurityConfig(HeaderFilter headerFilter,
                          @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri) {
        this.headerFilter = headerFilter;
        this.issuerUri = issuerUri;
    }


    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .pathMatchers("/api/v1/auth/*").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                ).build();
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        return ReactiveJwtDecoders.fromIssuerLocation(issuerUri);
    }


    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("dental-lab-service", r -> r
                        .path("/api/v1/**")
                        .filters(f -> f.filter(headerFilter))
                        .uri("lb://dental-lab-service"))
                .build();
    }
}

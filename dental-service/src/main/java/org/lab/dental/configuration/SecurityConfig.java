package org.lab.dental.configuration;

import jakarta.servlet.Filter;
import org.lab.dental.configuration.filter.HttpHeaderManagementFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final String issuerUri;

    @Autowired
    public SecurityConfig(@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri) {
        this.issuerUri = issuerUri;
    }


    public enum RequestPatternsPermitAll {
        ACTUATOR("/actuator/**"),
        OPEN_API("/docs/**", "/docs"),
        AUTHENTICATION("/api/v1/auth/**");

        public final String[] args;

        RequestPatternsPermitAll(String... args) {
            this.args = args;
        }
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        String[] REQUEST_PATTERNS_PERMIT_ALL = collectAllRequestPatterns();
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(REQUEST_PATTERNS_PERMIT_ALL).permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(jwtDecoder())))
                .addFilterAfter(httpHeaderManagementFilter(jwtDecoder()), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return JwtDecoders.fromIssuerLocation(issuerUri);
    }

    @Bean
    public Filter httpHeaderManagementFilter(JwtDecoder jwtDecoder) {
        return new HttpHeaderManagementFilter(jwtDecoder);
    }


    public static String[] collectAllRequestPatterns() {
        String[] REQUEST_PATTERNS_PERMIT_ALL = new String[countAllRequestPatternsLength()];
        int i = 0;
        for (RequestPatternsPermitAll e : RequestPatternsPermitAll.values()) {
            for (String arg : e.args) {
                REQUEST_PATTERNS_PERMIT_ALL[i] = arg;
                i++;
            }
        }
        return REQUEST_PATTERNS_PERMIT_ALL;
    }

    private static int countAllRequestPatternsLength() {
        int l = 0;
        for (RequestPatternsPermitAll e : RequestPatternsPermitAll.values()) {
            l += e.args.length;
        }
        return l;
    }
}
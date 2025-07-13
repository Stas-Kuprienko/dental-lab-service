package org.lab.ui_application.configuration;

import org.apache.tomcat.util.http.Rfc6265CookieProcessor;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/login", "/login/*", "/sing-up", "/sing-up/*", "/css/**", "/js/**").permitAll()
                        .anyRequest().authenticated()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .deleteCookies("refresh_token", "access_token")
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                ).build();
    }


    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> cookieCustomizer() {
        return factory -> factory.addContextCustomizers(context -> {
            final Rfc6265CookieProcessor cookieProcessor = new Rfc6265CookieProcessor();
            cookieProcessor.setSameSiteCookies("Strict");
            context.setCookieProcessor(cookieProcessor);
        });
    }
}
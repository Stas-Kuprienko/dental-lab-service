package org.lab.ui_application.configuration;

import org.dental.restclient.DentalLabRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import java.util.Locale;

@Configuration
public class UiApplicationConfig {


    @Bean
    public DentalLabRestClient dentalLabRestClient(@Value("${project.variables.dental-lab-url}") String url,
                                                   RestClient.Builder restClientBuilder) {
        return new DentalLabRestClient(url, restClientBuilder);
    }


    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver();
        resolver.setDefaultLocale(Locale.forLanguageTag("ru"));
        //TODO temporary
        resolver.setCookieName("lang");
        resolver.setCookieMaxAge(60 * 60 * 24 * 30); // 30 дней
        return resolver;
    }

}

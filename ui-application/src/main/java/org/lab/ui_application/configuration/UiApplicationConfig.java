package org.lab.ui_application.configuration;

import org.dental.restclient.DentalLabRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class UiApplicationConfig {


    @Bean
    public DentalLabRestClient dentalLabRestClient(@Value("${project.variables.dental-lab-url}") String url,
                                                   RestClient.Builder restClientBuilder) {
        return new DentalLabRestClient(url, restClientBuilder);
    }
}

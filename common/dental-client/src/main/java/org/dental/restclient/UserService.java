package org.dental.restclient;

import org.lab.model.User;
import org.lab.request.NewUser;
import org.springframework.web.client.RestClient;
import java.util.UUID;

public class UserService {

    private static final String RESOURCE = "/users";

    private final RestClient restClient;


    UserService(String baseUrl, RestClient.Builder restClientBuilder) {
        baseUrl += RESOURCE;
        restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }


    public User signUp(NewUser newUser) {
        return restClient
                .post()
                .body(newUser)
                .retrieve()
                .body(User.class);
    }

    public User find(UUID userId) {
        return restClient
                .get()
                .uri(DentalLabRestClient.uriById(userId))
                .retrieve()
                .body(User.class);
    }
}

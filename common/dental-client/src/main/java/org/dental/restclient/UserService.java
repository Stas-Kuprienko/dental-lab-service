package org.dental.restclient;

import org.lab.model.User;
import org.lab.request.NewUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

public class UserService {

    private static final String RESOURCE = "/user";

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

    public User get() {
        return restClient
                .get()
                .retrieve()
                .body(User.class);
    }

    public User updateName(String name) {
        return restClient
                .put()
                .body(name)
                .retrieve()
                .body(User.class);
    }

    public boolean delete() {
        ResponseEntity<Void> response = restClient
                .delete()
                .retrieve()
                .toBodilessEntity();
        return response.getStatusCode().is2xxSuccessful();
    }

    public boolean changeEmail() {
        return false;
    }

    public boolean verifyEmailChangeCode(String code) {
        return false;
    }
}

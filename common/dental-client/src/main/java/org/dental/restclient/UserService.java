package org.dental.restclient;

import org.lab.model.User;
import org.lab.request.NewUser;
import org.lab.request.UpdatePasswordRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

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

    public User get() {
        return restClient
                .get()
                .retrieve()
                .body(User.class);
    }

    public void updateName(String name) {
        restClient
                .put()
                .uri("/name")
                .body(name)
                .retrieve()
                .toBodilessEntity();
    }

    public void updateEmail(String email) {
        restClient
                .put()
                .uri("/email")
                .body(email)
                .retrieve()
                .toBodilessEntity();
    }

    public boolean updatePassword(UpdatePasswordRequest request) {
        return restClient
                .put()
                .uri("/password")
                .body(request)
                .retrieve()
                .toBodilessEntity()
                .getStatusCode()
                .is2xxSuccessful();
    }

    public void logout() {
        restClient
                .post()
                .uri("/logout")
                .retrieve()
                .toBodilessEntity();
    }

    public boolean delete() {
        ResponseEntity<Void> response = restClient
                .delete()
                .retrieve()
                .toBodilessEntity();
        return response.getStatusCode().is2xxSuccessful();
    }
}

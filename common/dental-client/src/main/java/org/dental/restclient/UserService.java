package org.dental.restclient;

import org.lab.enums.MailingType;
import org.lab.model.User;
import org.lab.request.NewUser;
import org.lab.request.UpdatePasswordRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import java.util.function.Consumer;

public class UserService {

    private static final String RESOURCE = "/users";
    private static final String NOTIFICATION_SUBSCRIBE = "/notification/subscribe";
    private static final String NOTIFICATION_UNSUBSCRIBE = "/notification/unsubscribe";

    private final RestClient restClient;


    UserService(RestClient restClient) {
        this.restClient = restClient;
    }


    public User signUp(NewUser newUser) {
        return restClient
                .post()
                .uri(RESOURCE)
                .body(newUser)
                .retrieve()
                .body(User.class);
    }

    public User get() {
        return restClient
                .get()
                .uri(RESOURCE)
                .retrieve()
                .body(User.class);
    }

    public User get(Consumer<HttpHeaders> headersConsumer) {
        return restClient
                .get()
                .uri(RESOURCE)
                .headers(headersConsumer)
                .retrieve()
                .body(User.class);
    }

    public void updateName(String name) {
        restClient
                .put()
                .uri(RESOURCE + "/name")
                .body(name)
                .retrieve()
                .toBodilessEntity();
    }

    public void updateEmail(String email) {
        restClient
                .put()
                .uri(RESOURCE + "/email")
                .body(email)
                .retrieve()
                .toBodilessEntity();
    }

    public boolean updatePassword(UpdatePasswordRequest request) {
        return restClient
                .put()
                .uri(RESOURCE + "/password")
                .body(request)
                .retrieve()
                .toBodilessEntity()
                .getStatusCode()
                .is2xxSuccessful();
    }

    public void logout() {
        restClient
                .post()
                .uri(RESOURCE + "/logout")
                .retrieve()
                .toBodilessEntity();
    }

    public boolean delete() {
        ResponseEntity<Void> response = restClient
                .delete()
                .uri(RESOURCE)
                .retrieve()
                .toBodilessEntity();
        return response.getStatusCode().is2xxSuccessful();
    }

    public boolean subscribeForNotifications(MailingType type) {
        ResponseEntity<Void> response = restClient
                .put()
                .uri(uriBuilder -> uriBuilder
                        .path(RESOURCE + NOTIFICATION_SUBSCRIBE)
                        .queryParam("type", type.name())
                        .build())
                .retrieve()
                .toBodilessEntity();
        return response.getStatusCode().is2xxSuccessful();
    }

    public boolean subscribeForNotifications(MailingType type, Consumer<HttpHeaders> headersConsumer) {
        ResponseEntity<Void> response = restClient
                .put()
                .uri(uriBuilder -> uriBuilder
                        .path(RESOURCE + NOTIFICATION_SUBSCRIBE)
                        .queryParam("type", type.name())
                        .build())
                .headers(headersConsumer)
                .retrieve()
                .toBodilessEntity();
        return response.getStatusCode().is2xxSuccessful();
    }

    public boolean unsubscribeForNotifications() {
        ResponseEntity<Void> response = restClient
                .put()
                .uri(RESOURCE + NOTIFICATION_UNSUBSCRIBE)
                .retrieve()
                .toBodilessEntity();
        return response.getStatusCode().is2xxSuccessful();
    }

    public boolean unsubscribeForNotifications(Consumer<HttpHeaders> headersConsumer) {
        ResponseEntity<Void> response = restClient
                .put()
                .uri(RESOURCE + NOTIFICATION_UNSUBSCRIBE)
                .headers(headersConsumer)
                .retrieve()
                .toBodilessEntity();
        return response.getStatusCode().is2xxSuccessful();
    }
}

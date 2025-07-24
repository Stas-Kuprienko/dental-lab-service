package org.dental.restclient;

import org.lab.model.TelegramChat;
import org.lab.request.NewTelegramChat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import java.util.Optional;

public class TelegramChatService {

    private static final String RESOURCE = "/telegram";

    private final RestClient restClient;


    TelegramChatService(String baseUrl, RestClient.Builder restClientBuilder) {
        baseUrl += RESOURCE;
        restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }


    public TelegramChat create(NewTelegramChat newTelegramChat) {
        return restClient
                .post()
                .body(newTelegramChat)
                .retrieve()
                .body(TelegramChat.class);
    }

    public Optional<TelegramChat> get(long chatId) {
        ResponseEntity<TelegramChat> response = restClient
                .get()
                .uri(DentalLabRestClient.uriById(chatId))
                .retrieve()
                .toEntity(TelegramChat.class);
        if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
            return Optional.of(response.getBody());
        } else {
            //TODO
            return Optional.empty();
        }
    }

}

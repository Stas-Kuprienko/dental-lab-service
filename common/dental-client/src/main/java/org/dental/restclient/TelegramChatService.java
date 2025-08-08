package org.dental.restclient;

import org.lab.model.TelegramChat;
import org.lab.request.NewTelegramOtpLink;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.util.Optional;
import java.util.UUID;

public class TelegramChatService {

    private static final String RESOURCE = "/telegram_chat";

    private final RestClient restClient;


    TelegramChatService(String baseUrl, RestClient.Builder restClientBuilder) {
        baseUrl += RESOURCE;
        restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }


    public void createLink(NewTelegramOtpLink newTelegramOtpLink) {
        //TODO
        restClient
                .post()
                .body(newTelegramOtpLink)
                .retrieve()
                .toBodilessEntity();
    }

    public void createLink(NewTelegramOtpLink newTelegramOtpLink, String headerKey, String headerValue) {
        //TODO
        restClient
                .post()
                .header(headerKey, headerValue)
                .body(newTelegramOtpLink)
                .retrieve()
                .toBodilessEntity();
    }

    public void setUserId(String key) {
        //TODO
        restClient
                .put()
                .uri(DentalLabRestClient.uriById(key))
                .retrieve()
                .toBodilessEntity();
    }

    public void setUserId(String key, String headerKey, String headerValue) {
        restClient
                .put()
                .uri(DentalLabRestClient.uriById(key))
                .header(headerKey, headerValue)
                .retrieve()
                .toBodilessEntity();
    }

    public String getOtpByKey(String key) {
        return restClient
                .get()
                .uri(DentalLabRestClient.uriById(key))
                .retrieve()
                .body(String.class);
    }

    public String getOtpByKey(String key, String headerKey, String headerValue) {
        return restClient
                .get()
                .uri(DentalLabRestClient.uriById(key))
                .header(headerKey, headerValue)
                .retrieve()
                .body(String.class);
    }

    public UUID bindTelegram(String key, String otp) {
        //TODO
        return restClient
                .post()
                .uri(DentalLabRestClient.uriById(key))
                .body(otp)
                .retrieve()
                .body(UUID.class);
    }

    public UUID bindTelegram(String key, String otp, String headerKey, String headerValue) {
        //TODO
        return restClient
                .post()
                .uri(DentalLabRestClient.uriById(key))
                .header(headerKey, headerValue)
                .body(otp)
                .retrieve()
                .body(UUID.class);
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

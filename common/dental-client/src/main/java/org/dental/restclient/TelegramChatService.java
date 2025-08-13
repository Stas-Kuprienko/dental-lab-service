package org.dental.restclient;

import org.lab.model.TelegramChat;
import org.lab.request.NewTelegramOtpLink;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

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

    public void createLink(NewTelegramOtpLink newTelegramOtpLink, Consumer<HttpHeaders> headersConsumer) {
        //TODO
        restClient
                .post()
                .headers(headersConsumer)
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

    public void setUserId(String key, Consumer<HttpHeaders> headersConsumer) {
        restClient
                .put()
                .uri(DentalLabRestClient.uriById(key))
                .headers(headersConsumer)
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

    public String getOtpByKey(String key, Consumer<HttpHeaders> headersConsumer) {
        return restClient
                .get()
                .uri(DentalLabRestClient.uriById(key))
                .headers(headersConsumer)
                .retrieve()
                .body(String.class);
    }

    public UUID bindTelegram(String key, String otp) {
        return restClient
                .post()
                .uri(DentalLabRestClient.uriById(key))
                .body(otp)
                .retrieve()
                .body(UUID.class);
    }

    public UUID bindTelegram(String key, String otp, Consumer<HttpHeaders> headersConsumer) {
        return restClient
                .post()
                .uri(DentalLabRestClient.uriById(key))
                .headers(headersConsumer)
                .body(otp)
                .retrieve()
                .body(UUID.class);
    }

    public Optional<TelegramChat> get(long chatId) {
        try {
            TelegramChat response = restClient
                    .get()
                    .uri(DentalLabRestClient.uriById(chatId))
                    .retrieve()
                    .toEntity(TelegramChat.class)
                    .getBody();
            return Optional.of(response);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    public Optional<TelegramChat> get(long chatId, Consumer<HttpHeaders> headersConsumer) {
        try {
            TelegramChat response = restClient
                    .get()
                    .uri(DentalLabRestClient.uriById(chatId))
                    .headers(headersConsumer)
                    .retrieve()
                    .toEntity(TelegramChat.class)
                    .getBody();
            return Optional.of(response);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }
}

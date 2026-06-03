package org.lab.dental.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.lab.dental.service.EventMessageService;
import org.lab.event.EventMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
public class TelegramBotRestService implements EventMessageService {

    private static final String url = "/api/v1/notifications";
    private static final int retries = 2;

    private final ExecutorService executorService;
    private final RestClient restClient;


    @Autowired
    public TelegramBotRestService(ExecutorService executorService,
                                  RestClient.Builder restClientBuilder,
                                  @Value("${project.variables.telegram-bot.url}") String baseUrl) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl + url)
                .build();
        this.executorService = executorService;
    }


    @Override
    public void send(EventMessage message) {
        CompletableFuture
                .supplyAsync(() -> restClient
                        .post()
                        .body(message)
                        .retrieve()
                        .body(String.class),
                        executorService)
                .thenAccept(v ->
                        log.info("The message '{}' was sent to Telegram-bot service successfully", message.getId())
                ).exceptionally(thr -> {
                    log.warn("Failure to send the message '%s' to Telegram-bot service, left %d retries".formatted(message.getId(), retries), thr);
                    retry(message, retries, thr);
                    return null;
                });
    }


    private void retry(EventMessage message, int i, Throwable throwable) {
        if (i > 0) {
            CompletableFuture
                    .supplyAsync(() -> restClient
                                    .post()
                                    .uri("")
                                    .body(message)
                                    .retrieve()
                                    .body(String.class),
                            executorService)
                    .thenAccept(v ->
                            log.info("The message '{}' was sent to Telegram-bot service successfully", message.getId())
                    ).exceptionally(thr -> {
                        log.warn("Failure to send the message '%s' to Telegram-bot service, left %d retries".formatted(message.getId(), i - 1), thr);
                        retry(message, i - 1, thr);
                        return null;
                    });
        } else {
            log.error("Failure to send the message '%s' to Telegram-bot service".formatted(message.getId()), throwable);
        }
    }
}

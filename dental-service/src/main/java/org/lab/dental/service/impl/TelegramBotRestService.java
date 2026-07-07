package org.lab.dental.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.lab.dental.service.EventMessageService;
import org.lab.event.EventMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.LockSupport;

@Slf4j
@Service
public class TelegramBotRestService implements EventMessageService {

    private static final String url = "/api/v1/notifications";
    private static final int retries = 2;

    private final ExecutorService executorService;
    private final RestClient restClient;


    @Autowired
    public TelegramBotRestService(@Qualifier("virtualThreadPerTaskExecutor") ExecutorService executorService,
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
                        executorService
                ).thenAccept(v ->
                        log.info("The message '{}' was sent to Telegram-bot service successfully", message.getId())
                ).exceptionally(thr -> {
                    int delaySeconds = 1;
                    LockSupport.parkNanos(Duration.of(delaySeconds, ChronoUnit.SECONDS).toNanos());
                    retry(message, retries, delaySeconds, thr);
                    return null;
                });
    }


    private void retry(EventMessage message, int retryI, int delaySeconds, Throwable throwable) {
        if (retryI > 0) {
            log.warn("Failure to send the message '%s' to Telegram-bot service, left %d retries".formatted(message.getId(), retryI), throwable);
            try {
                restClient
                        .post()
                        .uri("")
                        .body(message)
                        .retrieve()
                        .body(String.class);
                log.info("The message '{}' was sent to Telegram-bot service successfully", message.getId());
            } catch (Throwable thr) {
                delaySeconds++;
                LockSupport.parkNanos(Duration.of(delaySeconds, ChronoUnit.SECONDS).toNanos());
                retry(message, retryI - 1, delaySeconds, thr);
            }
        } else {
            log.error("Failure to send the message '%s' to Telegram-bot service".formatted(message.getId()), throwable);
        }
    }
}

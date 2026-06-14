package org.lab.telegram_bot.configuration;

import com.sun.net.httpserver.HttpServer;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.lab.telegram_bot.controller.TelegramBotController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;

@Slf4j
@Configuration
public class BotApiConfig {

    private final PrometheusMeterRegistry meterRegistry;
    private final ExecutorService executorService;
    private final int port;
    private BotSession botSession;
    private HttpServer httpServer;

    @Autowired
    public BotApiConfig(PrometheusMeterRegistry meterRegistry,
                        @Qualifier("virtualThreadPerTaskExecutor") ExecutorService executorService,
                        @Value("${server.port}") Integer port) {
        this.meterRegistry = meterRegistry;
        this.executorService = executorService;
        this.port = port;
    }


    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBotController botController) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        this.botSession = botsApi.registerBot(botController);
        log.info("TelegramBotsApi is registered");
        return botsApi;
    }

    @PostConstruct
    public void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/metrics", httpExchange -> {
            String response = meterRegistry.scrape();
            httpExchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = httpExchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });
        server.setExecutor(executorService);
        server.start();
        log.info("HttpServer for Prometheus metrics is started on port={}", port);
        this.httpServer = server;
    }

    @PreDestroy
    public void destroy() {
//        if (botSession != null) {
//            botSession.stop();
//        }
        if (httpServer != null) {
            httpServer.stop(0);
            log.info("HttpServer is stopped");
        }
    }
}

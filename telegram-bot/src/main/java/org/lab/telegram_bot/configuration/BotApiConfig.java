package org.lab.telegram_bot.configuration;

import org.lab.telegram_bot.controller.TelegramBotController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class BotApiConfig {

    private BotSession botSession;


    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBotController botController) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        this.botSession = botsApi.registerBot(botController);
        return botsApi;
    }

//    @PreDestroy
    public void destroy() {
        if (botSession != null) {
            botSession.stop();
        }
    }
}

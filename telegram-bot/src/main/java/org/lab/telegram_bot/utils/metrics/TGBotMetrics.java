package org.lab.telegram_bot.utils.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Getter
@Component
public class TGBotMetrics {

    private final Counter botStartCommands;
    private final Counter linkCreations;
    private final Counter linkLoginSuccesses;

    @Autowired
    public TGBotMetrics(MeterRegistry registry) {
        this.botStartCommands = Counter
                .builder("tg-bot.command.start")
                .description("Total start commands")
                .register(registry);
        this.linkCreations = Counter
                .builder("tg-bot.link.created")
                .description("Total created links")
                .register(registry);
        this.linkLoginSuccesses = Counter
                .builder("tg-bot.link.logins")
                .description("Total logins by link")
                .register(registry);
    }
}

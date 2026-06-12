package org.lab.dental.util.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ServiceMetrics {

    private final Counter userOkCreations;
    private final Counter userFailedCreations;
    private final Counter userOkDeletions;
    private final Counter userFailedDeletions;
    private final Counter otpEventFails;
    private final Counter telegramMailingFails;

    @Autowired
    public ServiceMetrics(MeterRegistry registry) {
        userOkCreations = Counter
                .builder("service.users.created")
                .description("Total created users")
                .register(registry);
        userFailedCreations = Counter
                .builder("service.users.failed-creation")
                .description("Total fails of user creation")
                .register(registry);
        userOkDeletions = Counter
                .builder("service.users.deletion")
                .description("Total deleted users")
                .register(registry);
        userFailedDeletions = Counter
                .builder("service.users.failed-deletion")
                .description("Total fails of user deletion")
                .register(registry);
        otpEventFails = Counter
                .builder("service.telegram.otp.fails")
                .description("Fails of OTP sending to Telegram")
                .register(registry);
        telegramMailingFails = Counter
                .builder("service.telegram.mailing.fails")
                .description("Fails of mailing sending to Telegram")
                .register(registry);
    }
}

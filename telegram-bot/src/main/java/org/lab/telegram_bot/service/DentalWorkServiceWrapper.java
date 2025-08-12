package org.lab.telegram_bot.service;

import org.dental.restclient.DentalWorkService;
import org.springframework.http.HttpHeaders;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class DentalWorkServiceWrapper {

    private final DentalWorkService dentalWorkService;
    private final Function<UUID, Consumer<HttpHeaders>> httpHeaderConsumerFunction;

    public DentalWorkServiceWrapper(DentalWorkService dentalWorkService, Function<UUID, Consumer<HttpHeaders>> httpHeaderConsumerFunction) {
        this.dentalWorkService = dentalWorkService;
        this.httpHeaderConsumerFunction = httpHeaderConsumerFunction;
    }



}

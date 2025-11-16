package org.lab.telegram_bot.service;

import org.dental.restclient.DentalWorkService;
import org.lab.model.DentalWork;
import org.lab.request.NewDentalWork;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import java.util.List;
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


    public DentalWork create(NewDentalWork newDentalWork, UUID userId) {
        return dentalWorkService.create(newDentalWork, httpHeaderConsumerFunction.apply(userId));
    }

    public DentalWork findById(long id, UUID userId) {
        return dentalWorkService.findById(id, httpHeaderConsumerFunction.apply(userId));
    }

    public List<DentalWork> findAll(UUID userId) {
        return dentalWorkService.findAll(httpHeaderConsumerFunction.apply(userId));
    }

    public List<DentalWork> findAllByMonth(int year, int month, UUID userId) {
        return dentalWorkService.findAllByMonth(year, month, httpHeaderConsumerFunction.apply(userId));
    }

    public List<DentalWork> searchDentalWorks(@Nullable String clinic, @Nullable String patient, UUID userId) {
        return dentalWorkService.searchDentalWorks(clinic, patient, httpHeaderConsumerFunction.apply(userId));
    }

    public DentalWork update(DentalWork updatable, UUID userId) {
        return dentalWorkService.update(updatable, httpHeaderConsumerFunction.apply(userId));
    }

    public void delete(long id, UUID userId) {
        dentalWorkService.delete(id, httpHeaderConsumerFunction.apply(userId));
    }

    public void sortForCompletion(boolean isPreviousMonth, UUID userId) {
        dentalWorkService.sortForCompletion(isPreviousMonth, httpHeaderConsumerFunction.apply(userId));
    }
}

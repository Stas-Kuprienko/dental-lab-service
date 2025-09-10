package org.lab.telegram_bot.datasource;

import org.lab.model.DentalWork;
import org.lab.telegram_bot.utils.DentalWorkList;
import java.util.Optional;
import java.util.UUID;

public interface DentalWorkRepository {

    void save(DentalWork dentalWork);

    void save(DentalWorkList dentalWorks);

    Optional<DentalWorkList> getAll(UUID userId);
}

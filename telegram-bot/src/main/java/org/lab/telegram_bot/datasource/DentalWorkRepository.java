package org.lab.telegram_bot.datasource;

import org.lab.model.DentalWork;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DentalWorkRepository {

    void save(List<DentalWork> dentalWorks, YearMonth yearMonth, UUID userId);

    void save(DentalWork dentalWork, YearMonth yearMonth);

    List<DentalWork> getAll(UUID userId, YearMonth yearMonth);

    Optional<DentalWork> getByIdAndUserId(long id, YearMonth yearMonth, UUID userId);

    void updateIfContains(DentalWork dentalWork, YearMonth yearMonth);

    void delete(long workId, YearMonth yearMonth, UUID userId);
}

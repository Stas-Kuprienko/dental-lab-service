package org.lab.dental.repository;

import org.lab.model.DentalWork;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DentalWorkCacheRepository {

    void save(List<DentalWork> dentalWorks, UUID userId);

    void save(DentalWork dentalWork);

    List<DentalWork> getAll(UUID userId);

    Optional<DentalWork> getByIdAndUserId(long id, UUID userId);

    void updateIfContains(DentalWork dentalWork);

    void delete(long workId, UUID userId);
}

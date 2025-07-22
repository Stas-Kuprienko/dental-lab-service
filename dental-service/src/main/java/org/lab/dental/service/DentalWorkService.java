package org.lab.dental.service;

import org.lab.dental.entity.DentalWorkEntity;
import org.lab.enums.WorkStatus;
import org.springframework.lang.Nullable;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

public interface DentalWorkService {

    DentalWorkEntity create(DentalWorkEntity dentalWork);

    DentalWorkEntity getByIdAndUserId(Long id, UUID userId);

    List<DentalWorkEntity> getAllForMonthByUserId(UUID userId, YearMonth yearMonth);

    List<DentalWorkEntity> getAllForCurrentMonthByUserId(UUID userId);

    List<DentalWorkEntity> getAllByClinicAndPatientAndUserId(UUID userId, @Nullable String clinic, @Nullable String patient);

    DentalWorkEntity update(DentalWorkEntity updatable);

    DentalWorkEntity updateStatus(Long id, UUID userId, WorkStatus status);

    DentalWorkEntity addProduct(Long id, UUID userId, UUID productTypeId, Integer quantity, LocalDate completeAt);

    DentalWorkEntity addProduct(DentalWorkEntity dentalWork, UUID productTypeId, Integer quantity, LocalDate completeAt);

    DentalWorkEntity updateProductCompletion(Long id, UUID userId, UUID productId, LocalDate completeAt);

    DentalWorkEntity deleteProduct(Long id, UUID userId, UUID productId);

    void delete(Long id, UUID userId);
}

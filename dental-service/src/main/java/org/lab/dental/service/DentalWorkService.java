package org.lab.dental.service;

import org.lab.dental.entity.DentalWorkEntity;
import org.lab.dental.entity.ProductEntity;
import org.lab.enums.WorkStatus;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

public interface DentalWorkService {

    DentalWorkEntity save(DentalWorkEntity dentalWork);

    DentalWorkEntity getByIdAndUserId(Long id, UUID userId);

    List<DentalWorkEntity> getAllForMonthByUserId(UUID userId, YearMonth yearMonth);

    List<DentalWorkEntity> getAllForCurrentMonthByUserId(UUID userId);

    List<DentalWorkEntity> getAllByPatientAndUserId(UUID userId, String patient);

    List<DentalWorkEntity> getAllByClinicAndUserId(UUID userId, String clinic);

    DentalWorkEntity update(DentalWorkEntity updatable);

    DentalWorkEntity updateStatus(Long id, UUID userId, WorkStatus status);

    DentalWorkEntity updateCompleteAt(Long id, UUID userId, LocalDate completeAt);

    DentalWorkEntity addProduct(Long id, UUID userId, ProductEntity product);

    DentalWorkEntity deleteProduct(Long id, UUID userId, UUID productId);

    void delete(Long id, UUID userId);
}

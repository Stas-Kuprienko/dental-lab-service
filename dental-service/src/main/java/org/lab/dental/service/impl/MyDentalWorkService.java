package org.lab.dental.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.lab.dental.entity.ProductEntity;
import org.lab.dental.exception.NotFoundCustomException;
import org.lab.dental.exception.PersistenceCustomException;
import org.lab.dental.entity.DentalWorkEntity;
import org.lab.dental.repository.DentalWorkRepository;
import org.lab.dental.repository.ProductRepository;
import org.lab.dental.service.DentalWorkService;
import org.lab.enums.WorkStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class MyDentalWorkService implements DentalWorkService {

    private final DentalWorkRepository dentalWorkRepository;
    private final ProductRepository productRepository;

    @Autowired
    public MyDentalWorkService(DentalWorkRepository dentalWorkRepository, ProductRepository productRepository) {
        this.dentalWorkRepository = dentalWorkRepository;
        this.productRepository = productRepository;
    }


    @Override
    public DentalWorkEntity create(DentalWorkEntity dentalWork) {
        if (dentalWork.getId() != null) {
            throw PersistenceCustomException.saveEntityWithId(dentalWork);
        }
        dentalWork.setAcceptedAt(LocalDate.now());
        log.info("Entity received to save: {}", dentalWork);
        DentalWorkEntity saved = dentalWorkRepository.save(dentalWork);
        log.info("Entity saved: {}", saved);
        return saved;
    }

    @Override
    public DentalWorkEntity getByIdAndUserId(Long id, UUID userId) {
        DentalWorkEntity dentalWork = dentalWorkRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> NotFoundCustomException
                        .byParams(DentalWorkEntity.class, Map.of("id", id, "userId", userId)));
        log.info("Entity is found: {}", dentalWork);
        return dentalWork;
    }

    @Override
    public List<DentalWorkEntity> getAllForMonthByUserId(UUID userId, YearMonth yearMonth) {
        LocalDate from = yearMonth.atDay(1);
        LocalDate to = yearMonth.atEndOfMonth();
        List<DentalWorkEntity> dentalWorks = dentalWorkRepository.findAllForMonthByUserId(userId, from, to);
        log.info("Found {} DentalWorks by parameters: userId='{}', yearMonth='{}'", dentalWorks.size(), userId, yearMonth);
        return dentalWorks;
    }

    @Override
    public List<DentalWorkEntity> getAllForCurrentMonthByUserId(UUID userId) {
        YearMonth yearMonth = YearMonth.now();
        LocalDate from = yearMonth.atDay(1);
        LocalDate to = yearMonth.atEndOfMonth();
        List<DentalWorkEntity> dentalWorks = dentalWorkRepository.findAllForMonthByUserId(userId, from, to);
        log.info("Found {} DentalWorks by parameters: userId='{}', yearMonth='{}'", dentalWorks.size(), userId, yearMonth);
        return dentalWorks;
    }

    @Override
    public List<DentalWorkEntity> getAllByClinicAndPatientAndUserId(UUID userId, @Nullable String clinic, @Nullable String patient) {
        List<DentalWorkEntity> dentalWorks;
        if (clinic == null && patient == null) {
            throw PersistenceCustomException.findByNullableParam(DentalWorkEntity.class, "clinic", "patient");
        } else if (clinic != null && patient != null) {
            dentalWorks =  dentalWorkRepository.findByClinicAndPatientAndUserId(userId, clinic, patient);
            log.info("Found {} DentalWorks by parameters: userId='{}', clinic='{}', patient='{}'", dentalWorks.size(), userId, clinic, patient);
        } else if (clinic != null) {
            dentalWorks =  dentalWorkRepository.findByClinicAndUserId(userId, clinic);
            log.info("Found {} DentalWorks by parameters: userId='{}', clinic='{}'", dentalWorks.size(), userId, clinic);
        } else {
            dentalWorks =  dentalWorkRepository.findByPatientAndUserId(userId, patient);
            log.info("Found {} DentalWorks by parameters: userId='{}', patient='{}'", dentalWorks.size(), userId, patient);
        }
        return dentalWorks;
    }

    @Override
    public DentalWorkEntity update(DentalWorkEntity updatable) {
        if (updatable.getId() == null) {
            throw PersistenceCustomException.updateEntityWithoutId(updatable);
        }
        log.info("Entity received to update: {}", updatable);
        DentalWorkEntity persisted = getByIdAndUserId(updatable.getId(), updatable.getUserId());
        persisted.setClinic(updatable.getClinic());
        persisted.setPatient(updatable.getPatient());
        persisted.setComment(updatable.getComment());
        persisted.setCompleteAt(updatable.getCompleteAt());
        persisted.setStatus(updatable.getStatus());
        DentalWorkEntity updated = dentalWorkRepository.save(persisted);
        log.info("Entity updated: {}", updated);
        return updated;
    }

    @Override
    public DentalWorkEntity updateStatus(Long id, UUID userId, WorkStatus status) {
        dentalWorkRepository.updateStatus(id, userId, status.name());
        log.info("Updated entity 'status': {}", status);
        return getByIdAndUserId(id, userId);
    }

    @Override
    public DentalWorkEntity updateCompleteAt(Long id, UUID userId, LocalDate completeAt) {
        dentalWorkRepository.updateCompleteAt(id, userId, completeAt);
        log.info("Updated entity 'completeAt': {}", completeAt);
        return getByIdAndUserId(id, userId);
    }

    @Override
    public DentalWorkEntity addProduct(Long id, UUID userId, ProductEntity product) {
        DentalWorkEntity dentalWork = getByIdAndUserId(id, userId);
        for (ProductEntity p : dentalWork.getProducts()) {
            if (p.getTitle().equals(product.getTitle())) {
                p.setQuantity(p.getQuantity() + product.getQuantity());
                ProductEntity updated = productRepository.save(p);
                log.info("Product '{}' updated for '{}'", updated, dentalWork);
                return dentalWork;
            }
        }
        dentalWork.getProducts().add(productRepository.save(product));
        log.info("Product '{}' added to '{}'", product, dentalWork);
        return dentalWork;
    }

    @Override
    public DentalWorkEntity deleteProduct(Long id, UUID userId, UUID productId) {
        productRepository.deleteById(productId);
        log.info("Product with ID='{}' is deleted for DentalWork with ID={} and userID='{}'", productId, id, userId);
        return getByIdAndUserId(id, userId);
    }

    @Override
    public void delete(Long id, UUID userId) {
        dentalWorkRepository.deleteByIdAndUserId(id, userId);
        log.info("DentalWork with ID={} and userID='{}' is deleted", id, userId);
    }
}

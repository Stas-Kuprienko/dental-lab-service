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
    public DentalWorkEntity save(DentalWorkEntity dentalWork) {
        if (dentalWork.getId() != null) {
            throw PersistenceCustomException.saveEntityWithId(dentalWork);
        }
        dentalWork.setAcceptedAt(LocalDate.now());
        log.info("Entity is received: {}", dentalWork);
        return dentalWorkRepository.save(dentalWork);
    }

    @Override
    public DentalWorkEntity getByIdAndUserId(Long id, UUID userId) {
        return dentalWorkRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> NotFoundCustomException
                        .byParams(DentalWorkEntity.class, Map.of("id", id, "userId", userId)));
    }

    @Override
    public List<DentalWorkEntity> getAllForMonthByUserId(UUID userId, YearMonth yearMonth) {
        LocalDate from = yearMonth.atDay(1);
        LocalDate to = yearMonth.atEndOfMonth();
        return dentalWorkRepository.findAllForMonthByUserId(userId, from, to);
    }

    @Override
    public List<DentalWorkEntity> getAllForCurrentMonthByUserId(UUID userId) {
        YearMonth yearMonth = YearMonth.now();
        LocalDate from = yearMonth.atDay(1);
        LocalDate to = yearMonth.atEndOfMonth();
        return dentalWorkRepository.findAllForMonthByUserId(userId, from, to);
    }

    @Override
    public List<DentalWorkEntity> getAllByPatientAndUserId(UUID userId, String patient) {
        return dentalWorkRepository.findByPatientAndUserId(userId, patient);
    }

    @Override
    public List<DentalWorkEntity> getAllByClinicAndUserId(UUID userId, String clinic) {
        return dentalWorkRepository.findByClinicAndUserId(userId, clinic);
    }

    @Override
    public DentalWorkEntity update(DentalWorkEntity updatable) {
        if (updatable.getId() == null) {
            throw PersistenceCustomException.updateEntityWithoutId(updatable);
        }
        log.info("Entity is received: {}", updatable);
        DentalWorkEntity persisted = getByIdAndUserId(updatable.getId(), updatable.getUserId());
        persisted.setClinic(updatable.getClinic());
        persisted.setPatient(updatable.getPatient());
        persisted.setComment(updatable.getComment());
        persisted.setCompleteAt(updatable.getCompleteAt());
        persisted.setStatus(updatable.getStatus());
        return dentalWorkRepository.save(persisted);
    }

    @Override
    public DentalWorkEntity updateStatus(Long id, UUID userId, WorkStatus status) {
        dentalWorkRepository.updateStatus(id, userId, status.name());
        return getByIdAndUserId(id, userId);
    }

    @Override
    public DentalWorkEntity updateCompleteAt(Long id, UUID userId, LocalDate completeAt) {
        dentalWorkRepository.updateCompleteAt(id, userId, completeAt);
        return getByIdAndUserId(id, userId);
    }

    @Override
    public DentalWorkEntity addProduct(Long id, UUID userId, ProductEntity product) {
        DentalWorkEntity dentalWork = getByIdAndUserId(id, userId);
        for (ProductEntity p : dentalWork.getProducts()) {
            if (p.getTitle().equals(product.getTitle())) {
                p.setQuantity(p.getQuantity() + product.getQuantity());
                productRepository.save(p);
                return dentalWork;
            }
        }
        dentalWork.getProducts().add(productRepository.save(product));
        return dentalWork;
    }

    @Override
    public DentalWorkEntity deleteProduct(Long id, UUID userId, UUID productId) {
        productRepository.deleteById(productId);
        return getByIdAndUserId(id, userId);
    }

    @Override
    public void delete(Long id, UUID userId) {
        dentalWorkRepository.deleteByIdAndUserId(id, userId);
    }
}

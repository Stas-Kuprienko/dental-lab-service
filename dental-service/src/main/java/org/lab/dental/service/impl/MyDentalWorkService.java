package org.lab.dental.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.lab.dental.entity.ProductEntity;
import org.lab.dental.entity.ProductTypeEntity;
import org.lab.dental.exception.NotFoundCustomException;
import org.lab.dental.exception.PersistenceCustomException;
import org.lab.dental.entity.DentalWorkEntity;
import org.lab.dental.repository.DentalWorkRepository;
import org.lab.dental.repository.ProductRepository;
import org.lab.dental.service.DentalWorkService;
import org.lab.dental.service.ProductTypeService;
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
    private final ProductTypeService productTypeService;

    @Autowired
    public MyDentalWorkService(DentalWorkRepository dentalWorkRepository,
                               ProductRepository productRepository,
                               ProductTypeService productTypeService) {
        this.dentalWorkRepository = dentalWorkRepository;
        this.productRepository = productRepository;
        this.productTypeService = productTypeService;
    }


    @Override
    public DentalWorkEntity create(DentalWorkEntity dentalWork) {
        if (dentalWork.getId() != null) {
            throw PersistenceCustomException.saveEntityWithId(dentalWork);
        }
        dentalWork.setAcceptedAt(LocalDate.now());
        dentalWork.setStatus(WorkStatus.MAKING);
        log.info("Entity received to save: {}", dentalWork);
        DentalWorkEntity saved = dentalWorkRepository.save(dentalWork);
        log.info("Entity saved: {}", saved);
        return saved;
    }

    @Override
    public DentalWorkEntity getByIdAndUserId(Long id, UUID userId) {
        DentalWorkEntity dentalWork = dentalWorkRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> NotFoundCustomException
                        .byParams("DentalWork", Map.of("id", id, "userId", userId)));
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
        List<DentalWorkEntity> dentalWorks = dentalWorkRepository.findAllFromMonthByUserId(userId, from);
        log.info("Found {} DentalWorks for current month by parameters: userId='{}'", dentalWorks.size(), userId);
        return dentalWorks;
    }

    @Override
    public List<DentalWorkEntity> getAllActualByUserId(UUID userId) {
        YearMonth yearMonth = YearMonth.now();
        LocalDate from = yearMonth.atDay(1);
        List<DentalWorkEntity> dentalWorks = dentalWorkRepository.findAllFromMonthAndNotPaidByUserId(userId, from);
        log.info("Found {} DentalWorks for current month by parameters: userId='{}'", dentalWorks.size(), userId);
        return dentalWorks;
    }

    @Override
    public List<DentalWorkEntity> getAllByClinicAndPatientAndUserId(UUID userId, @Nullable String clinic, @Nullable String patient) {
        List<DentalWorkEntity> dentalWorks;
        if (clinic == null && patient == null) {
            throw PersistenceCustomException.findByNullableParam("DentalWork", "clinic", "patient");
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
    public DentalWorkEntity addProduct(Long id, UUID userId, UUID productTypeId, Integer quantity, LocalDate completeAt) {
        if (productTypeId == null || quantity == null) {
            throw PersistenceCustomException.nullParameters("Product", "productTypeId", "quantity");
        }
        DentalWorkEntity dentalWork = getByIdAndUserId(id, userId);
        ProductTypeEntity productType = productTypeService.getByIdAndUserId(productTypeId, userId);
        ProductEntity product = ProductEntity.builder()
                .title(productType.getTitle())
                .price(productType.getPrice())
                .quantity(quantity)
                .dentalWorkId(id)
                .completeAt(completeAt)
                .acceptedAt(LocalDate.now())
                .build();
        for (ProductEntity p : dentalWork.getProducts()) {
            if (p.getTitle().equalsIgnoreCase(product.getTitle()) && p.getPrice().equals(product.getPrice())) {
                p.setQuantity(p.getQuantity() + product.getQuantity());
                p.setCompleteAt(completeAt);
                ProductEntity updated = productRepository.save(p);
                log.info("Product '{}' updated for '{}'", updated, dentalWork);
                setCompleteAtIfIsLater(dentalWork, completeAt);
                log.info("Product '{}' added to '{}'", product, dentalWork);
                return dentalWork;
            }
        }
        setCompleteAtIfIsLater(dentalWork, completeAt);
        dentalWork.getProducts().add(productRepository.save(product));
        log.info("Product '{}' added to '{}'", product, dentalWork);
        return dentalWork;
    }

    @Override
    public DentalWorkEntity addProduct(DentalWorkEntity dentalWork, UUID productTypeId, Integer quantity, LocalDate completeAt) {
        log.info("Entity received to add product: {}", dentalWork);
        if (productTypeId == null || quantity == null) {
            log.info("Nothing to add for entity: {}", dentalWork);
            return dentalWork;
        }
        ProductTypeEntity productType = productTypeService.getByIdAndUserId(productTypeId, dentalWork.getUserId());
        ProductEntity product = ProductEntity.builder()
                .dentalWorkId(dentalWork.getId())
                .title(productType.getTitle())
                .price(productType.getPrice())
                .quantity(quantity)
                .completeAt(completeAt)
                .acceptedAt(LocalDate.now())
                .build();
        product = productRepository.save(product);
        log.info("Product '{}' added to '{}'", product, dentalWork);
        setCompleteAtIfIsLater(dentalWork, completeAt);
        if (dentalWork.getProducts() == null) {
            dentalWork.setProducts(List.of(product));
        } else {
            dentalWork.getProducts().add(product);
        }
        return dentalWork;
    }

    @Override
    public DentalWorkEntity updateProductCompletion(Long id, UUID userId, UUID productId, LocalDate completeAt) {
        productRepository.updateCompleteAt(productId, id, completeAt);
        dentalWorkRepository.updateCompleteAt(id, userId, completeAt);
        log.info("Product with ID='{}' updated 'completeAt' on '{}' for DentalWork with ID={} and userID='{}'", productId, completeAt, id, userId);
        return getByIdAndUserId(id, userId);
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


    private void setCompleteAtIfIsLater(DentalWorkEntity dentalWork, LocalDate completeAt) {
        if (dentalWork.getCompleteAt().isBefore(completeAt)) {
            dentalWorkRepository.updateCompleteAt(dentalWork.getId(), dentalWork.getUserId(), completeAt);
            dentalWork.setCompleteAt(completeAt);
        }
    }
}

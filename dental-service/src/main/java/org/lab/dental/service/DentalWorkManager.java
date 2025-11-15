package org.lab.dental.service;

import org.lab.dental.entity.DentalWorkEntity;
import org.lab.dental.mapping.DentalWorkConverter;
import org.lab.dental.repository.DentalWorkCacheRepository;
import org.lab.enums.WorkStatus;
import org.lab.model.DentalWork;
import org.lab.request.NewDentalWork;
import org.lab.request.NewProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class DentalWorkManager {

    private final DentalWorkService dentalWorkService;
    private final DentalWorkConverter converter;
    private final DentalWorkCacheRepository cacheRepository;
    private final WorkPhotoFileService workPhotoFileService;


    @Autowired
    public DentalWorkManager(DentalWorkService dentalWorkService,
                             DentalWorkConverter converter,
                             DentalWorkCacheRepository cacheRepository,
                             WorkPhotoFileService workPhotoFileService) {
        this.dentalWorkService = dentalWorkService;
        this.converter = converter;
        this.cacheRepository = cacheRepository;
        this.workPhotoFileService = workPhotoFileService;
        workPhotoFileService.listenFileUploading(this::updateInCacheIfContains);
    }


    public DentalWork create(NewDentalWork newDentalWork, UUID userId) {
        DentalWorkEntity entity = converter.fromRequest(newDentalWork, userId);
        entity = dentalWorkService.create(entity);
        UUID productType = newDentalWork.getProductId();
        int quantity = newDentalWork.getQuantity();
        LocalDate completeAt = newDentalWork.getCompleteAt();
        entity = dentalWorkService.addProduct(entity, productType, quantity, completeAt);
        DentalWork dentalWork = converter.toDto(entity);
        if (dentalWork.getCompleteAt().getMonthValue() >= LocalDate.now().getMonthValue()) {
            cacheRepository.save(dentalWork);
        }
        return dentalWork;
    }

    public DentalWork getByIdAndUserId(long id, UUID userId) {
        Optional<DentalWork> optionalDentalWork = cacheRepository.getByIdAndUserId(id, userId);
        if (optionalDentalWork.isPresent()) {
            return optionalDentalWork.get();
        } else {
            DentalWorkEntity entity = dentalWorkService.getByIdAndUserId(id, userId);
            DentalWork dentalWork = converter.toDto(entity);
            List<String> photoFiles = workPhotoFileService.getAllFilenamesByWorkId(id);
            dentalWork.setPhotoFiles(photoFiles);
            return dentalWork;
        }
    }

    public List<DentalWork> getAllForMonthByUserId(UUID userId, YearMonth yearMonth) {
        List<DentalWorkEntity> entities = dentalWorkService.getAllForMonthByUserId(userId, yearMonth);
        return entities.stream()
                .map(converter::toDto)
                .toList();
    }

    public List<DentalWork> getAllForCurrentMonthByUserId(UUID userId) {
        List<DentalWorkEntity> entities = dentalWorkService.getAllForCurrentMonthByUserId(userId);
        return entities.stream()
                .map(converter::toDto)
                .toList();
    }

    public List<DentalWork> getAllActualByUserId(UUID userId) {
        List<DentalWork> dentalWorks = cacheRepository.getAll(userId);
        if (dentalWorks.isEmpty()) {
            List<DentalWorkEntity> entities = dentalWorkService.getAllActualByUserId(userId);
            dentalWorks = entities.stream()
                    .map(e -> {
                        DentalWork dw = converter.toDto(e);
                        dw.setPhotoFiles(workPhotoFileService.getAllFilenamesByWorkId(dw.getId()));
                        return dw;
                    })
                    .toList();
            cacheRepository.save(dentalWorks, userId);
        }
        return dentalWorks;
    }

    public List<DentalWork> getAllByClinicAndPatientAndUserId(UUID userId, String clinic, String patient) {
        List<DentalWorkEntity> entities = dentalWorkService.getAllByClinicAndPatientAndUserId(userId, clinic, patient);
        return entities.stream()
                .map(converter::toDto)
                .toList();
    }

    public DentalWork update(DentalWork updatable, long id) {
        updatable.setId(id);
        DentalWorkEntity entity = converter.toEntity(updatable);
        entity = dentalWorkService.update(entity);
        DentalWork dw = converter.toDto(entity);
        List<String> photoFiles = workPhotoFileService.getAllFilenamesByWorkId(dw.getId());
        dw.setPhotoFiles(photoFiles);
        cacheRepository.updateIfContains(dw);
        return dw;
    }

    public void updateStatus(long id, UUID userId, WorkStatus status) {
        dentalWorkService.updateStatus(id, userId, status);
        Optional<DentalWork> dentalWork = cacheRepository.getByIdAndUserId(id, userId);
        dentalWork.ifPresent(dw -> {
            dw.setStatus(status);
            cacheRepository.updateIfContains(dw);
        });
    }

    public DentalWork addProduct(long id, UUID userId, NewProduct newProduct) {
        UUID productTypeId = newProduct.getProductTypeId();
        int quantity = newProduct.getQuantity();
        LocalDate completeAt = newProduct.getCompleteAt();
        DentalWorkEntity entity = dentalWorkService.addProduct(id, userId, productTypeId, quantity, completeAt);
        DentalWork dentalWork = converter.toDto(entity);
        dentalWork.setPhotoFiles(workPhotoFileService.getAllFilenamesByWorkId(id));
        cacheRepository.updateIfContains(dentalWork);
        return dentalWork;
    }

    public DentalWork addProduct(DentalWork dentalWork, UUID productTypeId, Integer quantity, LocalDate completeAt) {
        DentalWorkEntity entity = converter.toEntity(dentalWork);
        entity = dentalWorkService.addProduct(entity, productTypeId, quantity, completeAt);
        dentalWork = converter.toDto(entity);
        dentalWork.setPhotoFiles(workPhotoFileService.getAllFilenamesByWorkId(dentalWork.getId()));
        cacheRepository.updateIfContains(dentalWork);
        return dentalWork;
    }

    public DentalWork updateProductCompletion(long id, UUID userId, UUID productId, LocalDate completeAt) {
        DentalWorkEntity entity = dentalWorkService.updateProductCompletion(id, userId, productId, completeAt);
        DentalWork dentalWork = converter.toDto(entity);
        dentalWork.setPhotoFiles(workPhotoFileService.getAllFilenamesByWorkId(id));
        cacheRepository.updateIfContains(dentalWork);
        return dentalWork;
    }

    public DentalWork deleteProduct(long id, UUID userId, UUID productId) {
        DentalWorkEntity entity = dentalWorkService.deleteProduct(id, userId, productId);
        DentalWork dentalWork = converter.toDto(entity);
        dentalWork.setPhotoFiles(workPhotoFileService.getAllFilenamesByWorkId(id));
        cacheRepository.updateIfContains(dentalWork);
        return dentalWork;
    }

    public void delete(long id, UUID userId) {
        List<String> filenames = workPhotoFileService.getAllFilenamesByWorkId(id);
        filenames.forEach(workPhotoFileService::deleteFile);
        cacheRepository.delete(id, userId);
        dentalWorkService.delete(id, userId);
    }


    private void updateInCacheIfContains(long id) {
        DentalWorkEntity entity = dentalWorkService.getById(id);
        DentalWork dentalWork = converter.toDto(entity);
        List<String> photoFiles = workPhotoFileService.getAllFilenamesByWorkId(id);
        dentalWork.setPhotoFiles(photoFiles);
        cacheRepository.updateIfContains(dentalWork);
    }
}

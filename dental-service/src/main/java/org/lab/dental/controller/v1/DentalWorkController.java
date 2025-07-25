package org.lab.dental.controller.v1;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.lab.dental.entity.DentalWorkEntity;
import org.lab.dental.mapping.DentalWorkConverter;
import org.lab.dental.service.DentalWorkService;
import org.lab.dental.util.RequestMappingReader;
import org.lab.dental.util.RequestParamsConverter;
import org.lab.enums.WorkStatus;
import org.lab.model.DentalWork;
import org.lab.request.NewDentalWork;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/dental_works")
public class DentalWorkController {

    private final String URL;

    private final DentalWorkService dentalWorkService;
    private final DentalWorkConverter dentalWorkConverter;


    @Autowired
    public DentalWorkController(DentalWorkService dentalWorkService, DentalWorkConverter dentalWorkConverter) {
        this.dentalWorkService = dentalWorkService;
        this.dentalWorkConverter = dentalWorkConverter;
        URL = RequestMappingReader.read(this.getClass());
    }


    @PostMapping
    public ResponseEntity<DentalWork> create(@RequestHeader("X-USER-ID") UUID userId,
                                             @RequestBody @Valid NewDentalWork newDentalWork) {

        log.info("From user '{}' received request: {}", userId, newDentalWork);
        DentalWorkEntity entity = dentalWorkConverter.fromRequest(newDentalWork, userId);
        entity = dentalWorkService.create(entity);
        UUID productType = newDentalWork.getProductId();
        Integer quantity = newDentalWork.getQuantity();
        LocalDate completeAt = newDentalWork.getCompleteAt();
        entity = dentalWorkService.addProduct(entity, productType, quantity, completeAt);
        return ResponseEntity
                .created(URI.create(URL + '/' + entity.getId())).body(dentalWorkConverter.toDto(entity));
    }


    @GetMapping("/{id}")
    public ResponseEntity<DentalWork> findById(@RequestHeader("X-USER-ID") UUID userId,
                                               @PathVariable("id") Long id) {

        log.info("From user '{}' received request with parameter: id={}", userId, id);
        DentalWorkEntity entity = dentalWorkService.getByIdAndUserId(id, userId);
        return ResponseEntity.ok(dentalWorkConverter.toDto(entity));
    }


    @GetMapping
    public List<DentalWork> findAllCurrentMonth(@RequestHeader("X-USER-ID") UUID userId) {

        log.info("From user '{}' received request to get all DentalWorks for current month", userId);
        List<DentalWorkEntity> entities = dentalWorkService.getAllForCurrentMonthByUserId(userId);
        return entities.stream().map(dentalWorkConverter::toDto).toList();
    }


    @GetMapping("/by-period")
    public List<DentalWork> findAllMonth(@RequestHeader("X-USER-ID") UUID userId,
                                         @RequestParam("year") Integer year,
                                         @RequestParam("month") Integer month) {

        log.info("From user '{}' received request with parameter: year={}, month={}", userId, year, month);
        List<DentalWorkEntity> entities = dentalWorkService
                .getAllForMonthByUserId(userId, RequestParamsConverter.converToYearMonth(year, month));
        return entities.stream().map(dentalWorkConverter::toDto).toList();
    }


    @GetMapping("/search")
    public List<DentalWork> findByClinicAndPatient(@RequestHeader("X-USER-ID") UUID userId,
                                                   @RequestParam(value = "clinic", required = false) String clinic,
                                                   @RequestParam(value = "patient", required = false) String patient) {

        log.info("From user '{}' received request with parameter: clinic={}, patient={}", userId, clinic, patient);
        List<DentalWorkEntity> entities = dentalWorkService.getAllByClinicAndPatientAndUserId(userId, clinic, patient);
        return entities.stream().map(dentalWorkConverter::toDto).toList();
    }


    @PutMapping("/{id}")
    public ResponseEntity<DentalWork> updateDentalWork(@RequestHeader("X-USER-ID") UUID userId,
                                                       @PathVariable("id") Long id,
                                                       @RequestBody @Valid DentalWork updatable) {

        log.info("From user '{}' received request: {}", userId, updatable);
        DentalWorkEntity entity = dentalWorkConverter.toEntity(updatable);
        entity.setId(id);
        entity = dentalWorkService.update(entity);
        return ResponseEntity.ok(dentalWorkConverter.toDto(entity));
    }


    @PatchMapping("/{id}/set-status-{status}")
    public ResponseEntity<DentalWork> updateStatus(@RequestHeader("X-USER-ID") UUID userId,
                                                   @PathVariable("id") Long id,
                                                   @PathVariable("status") WorkStatus status) {

        log.info("From user '{}' received request to set 'status': {}", userId, status);
        DentalWorkEntity dentalWork = dentalWorkService.updateStatus(id, userId, status);
        return ResponseEntity.ok(dentalWorkConverter.toDto(dentalWork));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDentalWork(@RequestHeader("X-USER-ID") UUID userId,
                                                 @PathVariable("id") Long id) {

        log.info("From user '{}' received request to delete by ID={}", userId, id);
        dentalWorkService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}

package org.lab.dental.controller.v1;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.lab.dental.service.DentalWorkManager;
import org.lab.dental.util.RequestMappingReader;
import org.lab.dental.util.RequestParamsConverter;
import org.lab.enums.WorkStatus;
import org.lab.model.DentalWork;
import org.lab.request.NewDentalWork;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/dental_works")
public class DentalWorkController {

    private final String URL;

    private final DentalWorkManager dentalWorkManager;


    @Autowired
    public DentalWorkController(DentalWorkManager dentalWorkManager) {
        this.dentalWorkManager = dentalWorkManager;
        URL = RequestMappingReader.read(this.getClass());
    }


    @PostMapping
    public ResponseEntity<DentalWork> create(@RequestHeader("X-USER-ID") UUID userId,
                                             @RequestBody @Valid NewDentalWork newDentalWork) {
        log.info("From user '{}' received request: {}", userId, newDentalWork);
        DentalWork dentalWork = dentalWorkManager.create(newDentalWork, userId);
        return ResponseEntity
                .created(URI.create(URL + '/' + dentalWork.getId())).body(dentalWork);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DentalWork> findById(@RequestHeader("X-USER-ID") UUID userId,
                                               @PathVariable("id") Long id) {
        log.info("From user '{}' received request with parameter: id={}", userId, id);
        DentalWork dentalWork = dentalWorkManager.getByIdAndUserId(id, userId);
        return ResponseEntity.ok(dentalWork);
    }

    @GetMapping
    public ResponseEntity<List<DentalWork>> findAllActualByUserId(@RequestHeader("X-USER-ID") UUID userId) {
        log.info("From user '{}' received request to get all DentalWorks for current month", userId);
        List<DentalWork> dentalWorks = dentalWorkManager.getAllActualByUserId(userId);
        return ResponseEntity.ok(dentalWorks);
    }

    @GetMapping("/by-period")
    public ResponseEntity<List<DentalWork>> findAllMonth(@RequestHeader("X-USER-ID") UUID userId,
                                                         @RequestParam("year") Integer year,
                                                         @RequestParam("month") Integer month) {
        log.info("From user '{}' received request with parameter: year={}, month={}", userId, year, month);
        List<DentalWork> dentalWorks = dentalWorkManager
                .getAllForMonthByUserId(userId, RequestParamsConverter.converToYearMonth(year, month));
        return ResponseEntity.ok(dentalWorks);
    }

    @GetMapping("/search")
    public ResponseEntity<List<DentalWork>> findByClinicAndPatient(@RequestHeader("X-USER-ID") UUID userId,
                                                                   @RequestParam(value = "clinic", required = false) String clinic,
                                                                   @RequestParam(value = "patient", required = false) String patient) {
        log.info("From user '{}' received request with parameter: clinic={}, patient={}", userId, clinic, patient);
        List<DentalWork> dentalWorks = dentalWorkManager.getAllByClinicAndPatientAndUserId(userId, clinic, patient);
        return ResponseEntity.ok(dentalWorks);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DentalWork> updateDentalWork(@RequestHeader("X-USER-ID") UUID userId,
                                                       @PathVariable("id") Long id,
                                                       @RequestBody @Valid DentalWork updatable) {
        log.info("From user '{}' received request: {}", userId, updatable);
        DentalWork dentalWork = dentalWorkManager.update(updatable, id);
        return ResponseEntity.ok(dentalWork);
    }

    @PatchMapping("/{id}/set-status-{status}")
    public ResponseEntity<Void> updateStatus(@RequestHeader("X-USER-ID") UUID userId,
                                             @PathVariable("id") Long id,
                                             @PathVariable("status") WorkStatus status) {
        log.info("From user '{}' received request to set 'status': {}", userId, status);
        dentalWorkManager.updateStatus(id, userId, status);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/set-status-{status}")
    public ResponseEntity<Void> updateStatus(@RequestHeader("X-USER-ID") UUID userId,
                                             @RequestBody List<Long> idList,
                                             @PathVariable("status") WorkStatus status) {
        log.info("From user '{}' received request to set 'status': {}", userId, status);
        dentalWorkManager.updateStatusForIdList(idList, userId, status);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDentalWork(@RequestHeader("X-USER-ID") UUID userId,
                                                 @PathVariable("id") Long id) {
        log.info("From user '{}' received request to delete by ID={}", userId, id);
        dentalWorkManager.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/sorting")
    public ResponseEntity<Void> sortForCompletion(@RequestHeader("X-USER-ID") UUID userId,
                                                              @RequestParam(name = "is_previous_month", defaultValue = "false") boolean isPreviousMonth) {
        log.info("From user '{}' received request to sorting", userId);
        dentalWorkManager.sortForCompletion(userId, isPreviousMonth);
        return ResponseEntity.ok().build();
    }
}

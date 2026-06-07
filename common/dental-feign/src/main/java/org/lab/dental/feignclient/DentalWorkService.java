package org.lab.dental.feignclient;

import org.lab.enums.WorkStatus;
import org.lab.model.DentalWork;
import org.lab.request.NewDentalWork;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@FeignClient(url = "${project.variables.dental-lab-api.url}", path = "/dental_works", name = "dental-work-service")
public interface DentalWorkService {


    @PostMapping
    DentalWork create(@RequestBody NewDentalWork newDentalWork);

    @PostMapping
    DentalWork create(@RequestBody NewDentalWork newDentalWork, @RequestHeader("X-USER-ID") UUID userId);

    @GetMapping("/{id}")
    DentalWork findById(@PathVariable("id") long id);

    @GetMapping("/{id}")
    DentalWork findById(@PathVariable("id") long id, @RequestHeader("X-USER-ID") UUID userId);

    @GetMapping
    List<DentalWork> findAllActualByUserId();

    @GetMapping
    List<DentalWork> findAllActualByUserId(@RequestHeader("X-USER-ID") UUID userId);

    @GetMapping("/by-period")
    List<DentalWork> findAllByMonth(@RequestParam("year") int year, @RequestParam("month") int month);

    @GetMapping("/by-period")
    List<DentalWork> findAllByMonth(@RequestParam("year") int year,
                                    @RequestParam("month") int month,
                                    @RequestHeader("X-USER-ID") UUID userId);

    @GetMapping("/search")
    List<DentalWork> findByClinicAndPatient(@RequestParam(value = "clinic", required = false) String clinic,
                                            @RequestParam(value = "patient", required = false) String patient);

    @GetMapping("/search")
    List<DentalWork> findByClinicAndPatient(@RequestParam(value = "clinic", required = false) String clinic,
                                            @RequestParam(value = "patient", required = false) String patient,
                                            @RequestHeader("X-USER-ID") UUID userId);

    @PutMapping("/{id}")
    DentalWork update(@PathVariable("id") long id, @RequestBody DentalWork updatable);

    @PutMapping("/{id}")
    DentalWork update(@PathVariable("id") long id,
                      @RequestBody DentalWork updatable,
                      @RequestHeader("X-USER-ID") UUID userId);

    @PatchMapping("/{id}/set-status-{status}")
    void updateStatus(@PathVariable("id") long id, @PathVariable("status") WorkStatus status);

    @PatchMapping("/{id}/set-status-{status}")
    void updateStatus(@PathVariable("id") long id,
                      @PathVariable("status") WorkStatus status,
                      @RequestHeader("X-USER-ID") UUID userId);

    @PatchMapping("/set-status-{status}")
    void updateStatus(@RequestBody List<Long> idList, @PathVariable("status") WorkStatus status);

    @PatchMapping("/set-status-{status}")
    void updateStatus(@RequestBody List<Long> idList,
                      @PathVariable("status") WorkStatus status,
                      @RequestHeader("X-USER-ID") UUID userId);

    @DeleteMapping("/{id}")
    void delete(@PathVariable("id") long id);

    @DeleteMapping("/{id}")
    void delete(@PathVariable("id") long id, @RequestHeader("X-USER-ID") UUID userId);

    @PutMapping("/sorting")
    void sortForCompletion(@RequestParam(name = "is_previous_month") boolean isPreviousMonth);

    @PutMapping("/sorting")
    void sortForCompletion(@RequestParam(name = "is_previous_month") boolean isPreviousMonth,
                           @RequestHeader("X-USER-ID") UUID userId);
}

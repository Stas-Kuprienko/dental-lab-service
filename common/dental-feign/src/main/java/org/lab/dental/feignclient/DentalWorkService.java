package org.lab.dental.feignclient;

import org.lab.enums.WorkStatus;
import org.lab.model.DentalWork;
import org.lab.request.NewDentalWork;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@FeignClient(url = "${project.variables.dental-lab-api.url}", path = "/dental_works", name = "dental-work-service")
public interface DentalWorkService {


    @PostMapping
    DentalWork create(@RequestBody NewDentalWork newDentalWork);

    @GetMapping("/{id}")
    DentalWork findById(@PathVariable("id") long id);

    @GetMapping
    List<DentalWork> findAllActualByUserId();

    @GetMapping("/by-period")
    List<DentalWork> findAllByMonth(@RequestParam("year") int year, @RequestParam("month") int month);

    @GetMapping("/search")
    List<DentalWork> findByClinicAndPatient(@RequestParam(value = "clinic", required = false) String clinic,
                                            @RequestParam(value = "patient", required = false) String patient);

    @PutMapping("/{id}")
    DentalWork update(@PathVariable("id") long id, @RequestBody DentalWork updatable);

    @PatchMapping("/{id}/set-status-{status}")
    void updateStatus(@PathVariable("id") long id, @PathVariable("status") WorkStatus status);

    @PatchMapping("/set-status-{status}")
    void updateStatus(@RequestBody List<Long> idList, @PathVariable("status") WorkStatus status);

    @DeleteMapping("/{id}")
    void delete(@PathVariable("id") long id);

    @PutMapping("/sorting")
    void sortForCompletion(@RequestParam(name = "is_previous_month") boolean isPreviousMonth);
}

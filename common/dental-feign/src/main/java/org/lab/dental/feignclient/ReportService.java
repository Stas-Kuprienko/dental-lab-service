package org.lab.dental.feignclient;

import org.lab.enums.WorkStatus;
import org.lab.model.DentalWork;
import org.lab.model.ProfitRecord;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@FeignClient(url = "${project.variables.dental-lab-api.url}", path = "/reports", name = "report-service")
public interface ReportService {


    @GetMapping("/works")
    byte[] downloadWorkReport(@RequestParam("year") int year, @RequestParam("month") int month);

    @GetMapping("/works")
    byte[] downloadWorkReport(@RequestParam("year") int year,
                              @RequestParam("month") int month,
                              @RequestHeader("X-USER-ID") UUID userId);

    @PostMapping(path = "/works", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    List<DentalWork> uploadReport(@RequestPart("file") MultipartFile file,
                                  @RequestParam("complete-at") YearMonth completeAt,
                                  @RequestParam("status") WorkStatus status);

    @PostMapping(path = "/works", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    List<DentalWork> uploadReport(@RequestPart("file") MultipartFile file,
                                  @RequestParam("complete-at") YearMonth completeAt,
                                  @RequestParam("status") WorkStatus status,
                                  @RequestHeader("X-USER-ID") UUID userId);

    @GetMapping("/profit")
    ProfitRecord countProfitForMonth(@RequestParam("year") int year, @RequestParam("month") int month);

    @GetMapping("/profit")
    ProfitRecord countProfitForMonth(@RequestParam("year") int year,
                                     @RequestParam("month") int month,
                                     @RequestHeader("X-USER-ID") UUID userId);
}

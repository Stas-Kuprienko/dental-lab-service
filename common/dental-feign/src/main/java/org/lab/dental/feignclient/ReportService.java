package org.lab.dental.feignclient;

import org.lab.model.ProfitRecord;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(url = "${project.variables.dental-lab-api.url}", path = "/reports", name = "report-service")
public interface ReportService {


    @GetMapping("/works")
    byte[] downloadWorkReport(@RequestParam("year") int year, @RequestParam("month") int month);

    @GetMapping("/works")
    byte[] downloadWorkReport(@RequestParam("year") int year, @RequestParam("month") int month, @RequestHeader("X-USER-ID") UUID userId);

    @GetMapping("/profit")
    ProfitRecord countProfitForMonth(@RequestParam("year") int year, @RequestParam("month") int month);

    @GetMapping("/profit")
    ProfitRecord countProfitForMonth(@RequestParam("year") int year, @RequestParam("month") int month, @RequestHeader("X-USER-ID") UUID userId);
}

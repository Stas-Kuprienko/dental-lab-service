package org.lab.dental.controller.v1;

import org.lab.dental.service.ReportService;
import org.lab.dental.util.RequestParamsConverter;
import org.lab.model.ProfitRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.ByteArrayInputStream;
import java.time.YearMonth;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }


    @GetMapping("/works")
    public ResponseEntity<Resource> downloadWorkReport(@RequestHeader("X-USER-ID") UUID userId,
                                                       @RequestParam int year,
                                                       @RequestParam int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        ByteArrayInputStream stream = reportService.createFile(userId, yearMonth);
        String fileName = yearMonth.getMonth() + "_" + yearMonth.getYear();
        return buildResponse(stream, fileName);
    }


    @GetMapping("/profit")
    public ResponseEntity<ProfitRecord> countProfitForMonth(@RequestHeader("X-USER-ID") UUID userId,
                                                             @RequestParam("year") Integer year,
                                                             @RequestParam("month") Integer month) {
        YearMonth yearMonth = RequestParamsConverter.converToYearMonth(year, month);
        ProfitRecord profitRecord = reportService.countProfits(userId, yearMonth);
        return ResponseEntity.ok(profitRecord);
    }


    private ResponseEntity<Resource> buildResponse(ByteArrayInputStream stream, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename + ".xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(stream));
    }
}

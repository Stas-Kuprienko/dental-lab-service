package org.lab.dental.controller.v1;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.lab.dental.service.DentalWorkManager;
import org.lab.dental.service.ReportService;
import org.lab.dental.util.RequestParamsConverter;
import org.lab.enums.WorkStatus;
import org.lab.model.DentalWork;
import org.lab.model.ProfitRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Slf4j
@Tag(name = "Reports")
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final DentalWorkManager dentalWorkManager;
    private final ReportService reportService;

    @Autowired
    public ReportController(DentalWorkManager dentalWorkManager, ReportService reportService) {
        this.dentalWorkManager = dentalWorkManager;
        this.reportService = reportService;
    }


    @GetMapping("/works")
    public ResponseEntity<Resource> downloadWorkReport(@RequestHeader("X-USER-ID") UUID userId,
                                                       @RequestParam int year,
                                                       @RequestParam int month) {
        log.info("From user '{}' received request to get report: {}", userId, year + "-" + month);
        YearMonth yearMonth = YearMonth.of(year, month);
        List<DentalWork> dentalWorks = dentalWorkManager.getAllForMonthByUserId(userId, yearMonth);
        ByteArrayInputStream stream = reportService.createFile(dentalWorks, userId, yearMonth);
        String fileName = yearMonth.getMonth() + "_" + yearMonth.getYear();
        return buildResponse(stream, fileName);
    }

    @PostMapping("/works")
    public ResponseEntity<List<DentalWork>> uploadReport(@RequestHeader("X-USER-ID") UUID userId,
                                                         @RequestParam("file") MultipartFile file,
                                                         @RequestParam("complete-at") YearMonth completeAt,
                                                         @RequestParam("status") WorkStatus status) {
        List<DentalWork> dentalWorks = reportService.readReport(file, userId, completeAt.atDay(1), status);
        dentalWorks = dentalWorkManager.createAll(dentalWorks);
        return ResponseEntity.ok(dentalWorks);
    }

    @GetMapping("/profit")
    public ResponseEntity<ProfitRecord> countProfitForMonth(@RequestHeader("X-USER-ID") UUID userId,
                                                            @RequestParam("year") Integer year,
                                                            @RequestParam("month") Integer month) {
        log.info("From user '{}' received request to count profit: {}", userId, year + "-" + month);
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

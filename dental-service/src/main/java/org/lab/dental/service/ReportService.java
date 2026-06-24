package org.lab.dental.service;

import org.lab.enums.WorkStatus;
import org.lab.model.DentalWork;
import org.lab.model.ProfitRecord;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

public interface ReportService {

    ByteArrayInputStream createFile(List<DentalWork> dentalWorks, UUID userId, YearMonth yearMonth);

    List<DentalWork> readReport(MultipartFile file, UUID userId, LocalDate completeAt, WorkStatus status);

    ProfitRecord countProfits(UUID userId, YearMonth month);

    List<ProfitRecord> countMonthlyProfit(UUID userId, int year);
}

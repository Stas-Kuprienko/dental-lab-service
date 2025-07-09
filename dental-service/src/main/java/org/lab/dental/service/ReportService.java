package org.lab.dental.service;

import org.lab.model.ProfitRecord;

import java.io.ByteArrayInputStream;
import java.time.YearMonth;
import java.util.UUID;

public interface ReportService {
    ByteArrayInputStream createFile(UUID userId, YearMonth yearMonth);

    ProfitRecord countProfits(UUID userId, YearMonth month);
}

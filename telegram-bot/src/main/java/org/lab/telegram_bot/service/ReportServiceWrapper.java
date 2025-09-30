package org.lab.telegram_bot.service;

import org.dental.restclient.ReportService;
import org.lab.model.ProfitRecord;
import org.springframework.http.HttpHeaders;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class ReportServiceWrapper {

    private final ReportService reportService;
    private final Function<UUID, Consumer<HttpHeaders>> httpHeaderConsumerFunction;

    public ReportServiceWrapper(ReportService reportService, Function<UUID, Consumer<HttpHeaders>> httpHeaderConsumerFunction) {
        this.reportService = reportService;
        this.httpHeaderConsumerFunction = httpHeaderConsumerFunction;
    }


    public byte[] downloadWorkReport(int year, int month, UUID userId) {
        return reportService.downloadWorkReport(year, month, httpHeaderConsumerFunction.apply(userId));
    }

    public ProfitRecord countProfitForMonth(int year, int month, UUID userId) {
        return reportService.countProfitForMonth(year, month, httpHeaderConsumerFunction.apply(userId));
    }
}

package org.lab.uimvc.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.dental.restclient.DentalLabRestClient;
import org.dental.restclient.ReportService;
import org.lab.model.ProfitRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Month;
import java.util.List;

@Controller
@RequestMapping("/main/reports")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(DentalLabRestClient dentalLabRestClient) {
        this.reportService = dentalLabRestClient.REPORTS;
    }


    @GetMapping("/works/download")
    public void downloadWorkList(@RequestParam int year, @RequestParam int month, HttpServletResponse response) throws IOException {
        byte[] fileBytes = reportService.downloadWorkReport(year, month);
        StringBuilder header = new StringBuilder("attachment; filename=");
        header.append(Month.of(month))
                .append('_')
                .append(year)
                .append(".xlsx");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", header.toString());
        response.setContentLength(fileBytes.length);
        try (OutputStream os = response.getOutputStream()) {
            os.write(fileBytes);
            os.flush();
        }
    }

    @GetMapping("/profit")
    public String profitListPage(@RequestParam int year, @RequestParam int month, Model model) {
        ProfitRecord record = reportService.countProfitForMonth(year, month);
        model.addAttribute("profit", List.of(record));
        return "profit-list";
    }
}

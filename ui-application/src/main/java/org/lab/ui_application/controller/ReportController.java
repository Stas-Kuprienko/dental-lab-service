package org.lab.ui_application.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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
import java.util.UUID;

@Controller
@RequestMapping("/main/reports")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(DentalLabRestClient dentalLabRestClient) {
        this.reportService = dentalLabRestClient.REPORTS;
    }


    @GetMapping("/works/download")
    public void downloadWorkList(@RequestParam int year, @RequestParam int month, HttpSession session, HttpServletResponse response) throws IOException {
        UUID userId = (UUID) session.getAttribute(MvcControllerUtil.ATTRIBUTE_KEY_USER_ID);
        byte[] fileBytes = reportService.downloadWorkReport(userId, year, month);
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
    public String profitListPage(@RequestParam int year, @RequestParam int month, HttpSession session, Model model) {
        UUID userId = (UUID) session.getAttribute(MvcControllerUtil.ATTRIBUTE_KEY_USER_ID);
        ProfitRecord record = reportService.countProfitForMonth(userId, year, month);
        model.addAttribute("profit", List.of(record));
        return "profit-list";
    }
}

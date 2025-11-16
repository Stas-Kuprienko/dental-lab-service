package org.lab.uimvc.controller;

import jakarta.servlet.http.HttpSession;
import org.dental.restclient.DentalLabRestClient;
import org.dental.restclient.DentalWorkService;
import org.lab.enums.WorkStatus;
import org.lab.model.DentalWork;
import org.lab.uimvc.service.ProductMapMvcService;
import org.lab.uimvc.util.HeaderMonth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/main/dental-works")
public class DentalWorkTableController {

    private static final String DENTAL_WORKS = "dental-works";

    private final ProductMapMvcService productMapService;
    private final DentalWorkService dentalWorkService;


    @Autowired
    public DentalWorkTableController(ProductMapMvcService productMapService, DentalLabRestClient dentalLabRestClient) {
        this.productMapService = productMapService;
        this.dentalWorkService = dentalLabRestClient.DENTAL_WORKS;
    }


    @GetMapping
    public String dentalWorks(@RequestParam(value = "year-month", required = false) String yearMonth,
                              HttpSession session, Model model) {
        MvcControllerUtil.addProductMapToModel(productMapService, session, model);
        HeaderMonth headerMonth = new HeaderMonth(yearMonth);
        List<DentalWork> works;
        if (headerMonth.isCurrentMonth()) {
            works = dentalWorkService.findAll();
        } else {
            works = dentalWorkService.findAllByMonth(headerMonth.getYear(), headerMonth.getMonthValue());
        }
        model.addAttribute("headerMonth", headerMonth);
        model.addAttribute("works", works);
        return DENTAL_WORKS;
    }

    @PostMapping("/search")
    public String searchDentalWorks(@RequestParam("clinic") String clinic, @RequestParam("patient") String patient,
                                    HttpSession session, Model model) {
        MvcControllerUtil.addProductMapToModel(productMapService, session, model);
        List<DentalWork> works = dentalWorkService.searchDentalWorks(clinic, patient);
        HeaderMonth headerMonth = new HeaderMonth(null);
        model.addAttribute("headerMonth", headerMonth);
        model.addAttribute("works", works);
        return DENTAL_WORKS;
    }

    @GetMapping("/bulk-update")
    public String bulkUpdatePage(@RequestParam("year-month") String yearMonth, Model model) {
        HeaderMonth headerMonth = new HeaderMonth(yearMonth);
        List<DentalWork> works;
        if (headerMonth.isCurrentMonth()) {
            works = dentalWorkService.findAll();
        } else {
            works = dentalWorkService.findAllByMonth(headerMonth.getYear(), headerMonth.getMonthValue());
        }
        model.addAttribute("headerMonth", headerMonth);
        model.addAttribute("works", works);
        return "bulk-update";
    }

    @PostMapping("/bulk-update")
    public String bulkUpdateHandle(@RequestParam("ids") List<Long> ids,
                                   @RequestParam("status") WorkStatus status,
                                   @RequestParam("year-month") String yearMonth) {
        dentalWorkService.updateStatus(ids, status);
        return MvcControllerUtil.REDIRECT + "/main/dental-works?year-month=" + yearMonth;
    }

    @PostMapping("/sorting")
    public String sortForCompletion(@RequestParam("is_previous_month") boolean isPreviousMonth) {
        dentalWorkService.sortForCompletion(isPreviousMonth);
        return MvcControllerUtil.REDIRECT + "/main/dental-works";
    }
}

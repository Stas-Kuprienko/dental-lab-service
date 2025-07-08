package org.lab.ui_application.controller;

import jakarta.servlet.http.HttpSession;
import org.dental.restclient.DentalLabRestClient;
import org.dental.restclient.DentalWorkService;
import org.dental.restclient.ProductMapService;
import org.lab.model.DentalWork;
import org.lab.model.ProductType;
import org.lab.ui_application.util.HeaderMonth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/main/dental-works")
public class DentalWorkTableController {

    private static final String ATTRIBUTE_KEY_MAP = "map";
    private static final String DENTAL_WORKS = "dental-works";

    private final ProductMapService productMapService;
    private final DentalWorkService dentalWorkService;


    @Autowired
    public DentalWorkTableController(DentalLabRestClient dentalLabRestClient) {
        this.productMapService = dentalLabRestClient.PRODUCT_MAP;
        this.dentalWorkService = dentalLabRestClient.DENTAL_WORKS;
    }


    @GetMapping
    public String dentalWorks(@RequestParam(value = "year-month", required = false) String yearMonth,
                              HttpSession session, Model model) {
        UUID userId = UUID.fromString("30ac0d36-cd43-4083-9494-f2b37b12dc9c");
        @SuppressWarnings("unchecked")
        List<ProductType> items = (List<ProductType>) session.getAttribute(ATTRIBUTE_KEY_MAP);
        if (items == null) {
            items = productMapService.findAll(userId).getEntries();
            session.setAttribute(ATTRIBUTE_KEY_MAP, items);
        }
        model.addAttribute(ATTRIBUTE_KEY_MAP, items);
        HeaderMonth headerMonth = new HeaderMonth(yearMonth);
        List<DentalWork> works;
        if (headerMonth.isCurrentMonth()) {
            works = dentalWorkService.findAll(userId);
        } else {
            works = dentalWorkService.findAllByMonth(userId, headerMonth.getYear(), headerMonth.getMonthValue());
        }
        model.addAttribute("headerMonth", headerMonth);
        model.addAttribute("works", works);
        return DENTAL_WORKS;
    }

    @PostMapping("/search")
    public String searchDentalWorks(@RequestParam("clinic") String clinic, @RequestParam("patient") String patient,
                                    HttpSession session, Model model) {
        UUID userId = UUID.fromString("30ac0d36-cd43-4083-9494-f2b37b12dc9c");
        @SuppressWarnings("unchecked")
        List<ProductType> items = (List<ProductType>) session.getAttribute(ATTRIBUTE_KEY_MAP);
        if (items == null) {
            items = productMapService.findAll(userId).getEntries();
            session.setAttribute(ATTRIBUTE_KEY_MAP, items);
        }
        model.addAttribute(ATTRIBUTE_KEY_MAP, items);
        List<DentalWork> works = dentalWorkService.searchDentalWorks(userId, clinic, patient);
        HeaderMonth headerMonth = new HeaderMonth(null);
        model.addAttribute("headerMonth", headerMonth);
        model.addAttribute("works", works);
        return DENTAL_WORKS;
    }
}

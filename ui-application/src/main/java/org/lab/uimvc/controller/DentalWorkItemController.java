package org.lab.uimvc.controller;

import jakarta.servlet.http.HttpSession;
import org.dental.restclient.DentalLabRestClient;
import org.dental.restclient.DentalWorkService;
import org.dental.restclient.ProductService;
import org.lab.model.DentalWork;
import org.lab.model.ProductMap;
import org.lab.request.NewDentalWork;
import org.lab.request.NewProduct;
import org.lab.uimvc.service.ProductMapMvcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@Controller
@RequestMapping("/main/dental-works")
public class DentalWorkItemController {

    private static final String ATTRIBUTE_KEY_MAP = "map";
    private static final String VIEW_DENTAL_WORK = "view-dental-work";

    private final ProductMapMvcService productMapService;
    private final DentalWorkService dentalWorkService;
    private final ProductService productService;


    @Autowired
    public DentalWorkItemController(ProductMapMvcService productMapService, DentalLabRestClient dentalLabRestClient) {
        this.productMapService = productMapService;
        this.dentalWorkService = dentalLabRestClient.DENTAL_WORKS;
        this.productService = dentalLabRestClient.PRODUCTS;
    }


    @GetMapping("/new")
    public String newWorkForm(HttpSession session, Model model) {
        MvcControllerUtil.addProductMapToModel(productMapService, session, model);
        return "new-dental-work";
    }

    @PostMapping
    public String createDentalWork(@ModelAttribute NewDentalWork newDentalWork, Model model, HttpSession session) {
        DentalWork work = dentalWorkService.create(newDentalWork);
        model.addAttribute("work", work);
        MvcControllerUtil.addProductMapToModel(productMapService, session, model);
        return MvcControllerUtil.REDIRECT + VIEW_DENTAL_WORK;
    }

    @GetMapping("/{id}")
    public String viewDentalWork(@PathVariable("id") Long id, HttpSession session, Model model) {
        MvcControllerUtil.addProductMapToModel(productMapService, session, model);
        DentalWork work = dentalWorkService.findById(id);
        model.addAttribute("work", work);
        return VIEW_DENTAL_WORK;
    }

    @PostMapping("/{id}/products/new")
    public String addProduct(@PathVariable("id") Long id, @ModelAttribute NewProduct newProduct, HttpSession session, Model model) {
        DentalWork work = productService.addProduct(id, newProduct);
        model.addAttribute("work", work);
        MvcControllerUtil.addProductMapToModel(productMapService, session, model);
        return MvcControllerUtil.REDIRECT + VIEW_DENTAL_WORK;
    }

    @PostMapping("/{id}/edit")
    public String updateDentalWork(@PathVariable("id") Long id, @ModelAttribute DentalWork dentalWork, HttpSession session, Model model) {
        MvcControllerUtil.addProductMapToModel(productMapService, session, model);
        dentalWork.setId(id);
        DentalWork updated = dentalWorkService.update(dentalWork);
        model.addAttribute("work", updated);
        return MvcControllerUtil.REDIRECT + VIEW_DENTAL_WORK;
    }

    @PostMapping("/{id}/delete")
    public String deleteDentalWork(@PathVariable("id") Long id, HttpSession session, Model model) {
        dentalWorkService.delete(id);
        UUID userId = (UUID) session.getAttribute(MvcControllerUtil.ATTRIBUTE_KEY_USER_ID);
        ProductMap map = productMapService.get(userId);
        model.addAttribute(ATTRIBUTE_KEY_MAP, map.getEntries());
        return MvcControllerUtil.REDIRECT + "dental-works";
    }
}

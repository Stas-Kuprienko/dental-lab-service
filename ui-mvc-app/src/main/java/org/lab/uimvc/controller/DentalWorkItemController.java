package org.lab.uimvc.controller;

import jakarta.servlet.http.HttpSession;
import org.lab.dental.feignclient.DentalWorkService;
import org.lab.dental.feignclient.ProductService;
import org.lab.model.DentalWork;
import org.lab.request.NewDentalWork;
import org.lab.request.NewProduct;
import org.lab.uimvc.service.ProductMapMvcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.UUID;

@Controller
@RequestMapping("/main/dental-works")
public class DentalWorkItemController {

    private static final String DENTAL_WORKS = MvcControllerUtil.MAIN_PATH + "/dental-works";
    private static final String DENTAL_WORKS_BY_ID = MvcControllerUtil.MAIN_PATH + "/dental-works/%d";
    private static final String ATTRIBUTE_WORK = "work";

    private final ProductMapMvcService productMapService;
    private final DentalWorkService dentalWorkService;
    private final ProductService productService;


    @Autowired
    public DentalWorkItemController(ProductMapMvcService productMapService,
                                    DentalWorkService dentalWorkService,
                                    ProductService productService) {
        this.productMapService = productMapService;
        this.dentalWorkService = dentalWorkService;
        this.productService = productService;
    }


    @GetMapping("/new")
    public String newWorkForm(HttpSession session, Model model) {
        MvcControllerUtil.addProductMapToModel(productMapService, session, model);
        return "new-dental-work";
    }

    @PostMapping
    public String createDentalWork(@ModelAttribute NewDentalWork newDentalWork, Model model, HttpSession session) {
        DentalWork work = dentalWorkService.create(newDentalWork);
        model.addAttribute(ATTRIBUTE_WORK, work);
        MvcControllerUtil.addProductMapToModel(productMapService, session, model);
        return MvcControllerUtil.REDIRECT + DENTAL_WORKS_BY_ID.formatted(work.getId());
    }

    @GetMapping("/{id}")
    public String viewDentalWork(@PathVariable("id") Long id, HttpSession session, Model model) {
        MvcControllerUtil.addProductMapToModel(productMapService, session, model);
        DentalWork work = dentalWorkService.findById(id);
        model.addAttribute(ATTRIBUTE_WORK, work);
        return "view-dental-work";
    }

    @PostMapping("/{id}/products/new")
    public String addProduct(@PathVariable("id") Long id,
                             @ModelAttribute NewProduct newProduct,
                             HttpSession session, Model model) {

        DentalWork work = productService.addProduct(id, newProduct);
        model.addAttribute(ATTRIBUTE_WORK, work);
        MvcControllerUtil.addProductMapToModel(productMapService, session, model);
        return MvcControllerUtil.REDIRECT + DENTAL_WORKS_BY_ID.formatted(work.getId());
    }

    @PostMapping("/{id}/products/update")
    public String updateCompletion(@PathVariable("id") Long id,
                                   @RequestParam("product") UUID productId,
                                   @RequestParam("completeAt") LocalDate completeAt,
                                   HttpSession session, Model model) {

        DentalWork work = productService.updateCompletion(id, productId, completeAt);
        model.addAttribute(ATTRIBUTE_WORK, work);
        MvcControllerUtil.addProductMapToModel(productMapService, session, model);
        return MvcControllerUtil.REDIRECT + DENTAL_WORKS_BY_ID.formatted(work.getId());
    }

    @PostMapping("/{id}/products/delete")
    public String deleteProduct(@PathVariable("id") Long id,
                                @RequestParam("product") UUID productId,
                                HttpSession session, Model model) {

        DentalWork work = productService.deleteProduct(id, productId);
        model.addAttribute(ATTRIBUTE_WORK, work);
        MvcControllerUtil.addProductMapToModel(productMapService, session, model);
        return MvcControllerUtil.REDIRECT + DENTAL_WORKS_BY_ID.formatted(work.getId());
    }

    @PostMapping("/{id}/edit")
    public String updateDentalWork(@PathVariable("id") Long id,
                                   @ModelAttribute DentalWork dentalWork,
                                   HttpSession session, Model model) {

        MvcControllerUtil.addProductMapToModel(productMapService, session, model);
        DentalWork updated = dentalWorkService.update(id, dentalWork);
        model.addAttribute(ATTRIBUTE_WORK, updated);
        return MvcControllerUtil.REDIRECT + DENTAL_WORKS_BY_ID.formatted(id);
    }

    @PostMapping("/{id}/delete")
    public String deleteDentalWork(@PathVariable("id") Long id, HttpSession session, Model model) {
        dentalWorkService.delete(id);
        MvcControllerUtil.addProductMapToModel(productMapService, session, model);
        return MvcControllerUtil.REDIRECT + DENTAL_WORKS;
    }
}

package org.lab.uimvc.controller;

import org.lab.dental.feignclient.DentalWorkService;
import org.lab.dental.feignclient.ProductMapService;
import org.lab.dental.feignclient.ProductService;
import org.lab.model.DentalWork;
import org.lab.request.NewDentalWork;
import org.lab.request.NewProduct;
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

    private final ProductMapService productMapService;
    private final DentalWorkService dentalWorkService;
    private final ProductService productService;


    @Autowired
    public DentalWorkItemController(ProductMapService productMapService,
                                    DentalWorkService dentalWorkService,
                                    ProductService productService) {
        this.productMapService = productMapService;
        this.dentalWorkService = dentalWorkService;
        this.productService = productService;
    }


    @GetMapping("/new")
    public String newWorkForm(Model model) {
        MvcControllerUtil.addProductMapToModel(productMapService, model);
        return "new-dental-work";
    }

    @PostMapping
    public String createDentalWork(@ModelAttribute NewDentalWork newDentalWork, Model model) {
        DentalWork work = dentalWorkService.create(newDentalWork);
        model.addAttribute(ATTRIBUTE_WORK, work);
        MvcControllerUtil.addProductMapToModel(productMapService, model);
        return MvcControllerUtil.REDIRECT + DENTAL_WORKS_BY_ID.formatted(work.getId());
    }

    @GetMapping("/{id}")
    public String viewDentalWork(@PathVariable("id") Long id, Model model) {
        MvcControllerUtil.addProductMapToModel(productMapService, model);
        DentalWork work = dentalWorkService.findById(id);
        model.addAttribute(ATTRIBUTE_WORK, work);
        return "view-dental-work";
    }

    @PostMapping("/{id}/products/new")
    public String addProduct(@PathVariable("id") Long id,
                             @ModelAttribute NewProduct newProduct,
                             Model model) {

        DentalWork work = productService.addProduct(id, newProduct);
        model.addAttribute(ATTRIBUTE_WORK, work);
        MvcControllerUtil.addProductMapToModel(productMapService, model);
        return MvcControllerUtil.REDIRECT + DENTAL_WORKS_BY_ID.formatted(work.getId());
    }

    @PostMapping("/{id}/products/update")
    public String updateCompletion(@PathVariable("id") Long id,
                                   @RequestParam("product") UUID productId,
                                   @RequestParam("completeAt") LocalDate completeAt,
                                   Model model) {

        DentalWork work = productService.updateCompletion(id, productId, completeAt);
        model.addAttribute(ATTRIBUTE_WORK, work);
        MvcControllerUtil.addProductMapToModel(productMapService, model);
        return MvcControllerUtil.REDIRECT + DENTAL_WORKS_BY_ID.formatted(work.getId());
    }

    @PostMapping("/{id}/products/delete")
    public String deleteProduct(@PathVariable("id") Long id,
                                @RequestParam("product") UUID productId,
                                Model model) {

        DentalWork work = productService.deleteProduct(id, productId);
        model.addAttribute(ATTRIBUTE_WORK, work);
        MvcControllerUtil.addProductMapToModel(productMapService, model);
        return MvcControllerUtil.REDIRECT + DENTAL_WORKS_BY_ID.formatted(work.getId());
    }

    @PostMapping("/{id}/edit")
    public String updateDentalWork(@PathVariable("id") Long id,
                                   @ModelAttribute DentalWork dentalWork,
                                   Model model) {

        MvcControllerUtil.addProductMapToModel(productMapService, model);
        DentalWork updated = dentalWorkService.update(id, dentalWork);
        model.addAttribute(ATTRIBUTE_WORK, updated);
        return MvcControllerUtil.REDIRECT + DENTAL_WORKS_BY_ID.formatted(id);
    }

    @PostMapping("/{id}/delete")
    public String deleteDentalWork(@PathVariable("id") Long id, Model model) {
        dentalWorkService.delete(id);
        MvcControllerUtil.addProductMapToModel(productMapService, model);
        return MvcControllerUtil.REDIRECT + DENTAL_WORKS;
    }
}

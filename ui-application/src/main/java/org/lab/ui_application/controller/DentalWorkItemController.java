package org.lab.ui_application.controller;

import jakarta.servlet.http.HttpSession;
import org.dental.restclient.DentalLabRestClient;
import org.dental.restclient.DentalWorkService;
import org.dental.restclient.ProductMapService;
import org.dental.restclient.ProductService;
import org.lab.model.DentalWork;
import org.lab.model.ProductType;
import org.lab.request.NewDentalWork;
import org.lab.request.NewProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/main/dental-works")
public class DentalWorkItemController {

    private static final String ATTRIBUTE_KEY_MAP = "map";
    private static final String VIEW_DENTAL_WORK = "view-dental-work";

    private final ProductMapService productMapService;
    private final DentalWorkService dentalWorkService;
    private final ProductService productService;


    @Autowired
    public DentalWorkItemController(DentalLabRestClient dentalLabRestClient) {
        productMapService = dentalLabRestClient.PRODUCT_MAP;
        dentalWorkService = dentalLabRestClient.DENTAL_WORKS;
        productService = dentalLabRestClient.PRODUCTS;
    }


    @GetMapping("/new")
    public String newWorkForm(HttpSession session, Model model) {
        @SuppressWarnings("unchecked")
        List<ProductType> items = (List<ProductType>) session.getAttribute(ATTRIBUTE_KEY_MAP);
        if (items == null) {
            items = productMapService.findAll().getEntries();
            session.setAttribute(ATTRIBUTE_KEY_MAP, items);
        }
        model.addAttribute(ATTRIBUTE_KEY_MAP, items);
        return "new-dental-work";
    }

    @PostMapping
    public String createDentalWork(@ModelAttribute NewDentalWork newDentalWork, Model model, HttpSession session) {
        DentalWork work = dentalWorkService.create(newDentalWork);
        model.addAttribute("work", work);
        @SuppressWarnings("unchecked")
        List<ProductType> items = (List<ProductType>) session.getAttribute(ATTRIBUTE_KEY_MAP);
        if (items == null) {
            items = productMapService.findAll().getEntries();
            session.setAttribute(ATTRIBUTE_KEY_MAP, items);
        }
        model.addAttribute(ATTRIBUTE_KEY_MAP, items);
        return VIEW_DENTAL_WORK;
    }

    @GetMapping("/{id}")
    public String viewDentalWork(@PathVariable("id") Long id, HttpSession session, Model model) {
        @SuppressWarnings("unchecked")
        List<ProductType> items = (List<ProductType>) session.getAttribute(ATTRIBUTE_KEY_MAP);
        if (items == null) {
            items = productMapService.findAll().getEntries();
            session.setAttribute(ATTRIBUTE_KEY_MAP, items);
        }
        model.addAttribute(ATTRIBUTE_KEY_MAP, items);
        DentalWork work = dentalWorkService.findById(id);
        model.addAttribute("work", work);
        return VIEW_DENTAL_WORK;
    }

    @PostMapping("/{id}/products/new")
    public String addProduct(@PathVariable("id") Long id, @ModelAttribute NewProduct newProduct, HttpSession session, Model model) {
        DentalWork work = productService.addProduct(id, newProduct);
        model.addAttribute("work", work);
        @SuppressWarnings("unchecked")
        List<ProductType> items = (List<ProductType>) session.getAttribute(ATTRIBUTE_KEY_MAP);
        if (items == null) {
            items = productMapService.findAll().getEntries();
            session.setAttribute(ATTRIBUTE_KEY_MAP, items);
        }
        model.addAttribute(ATTRIBUTE_KEY_MAP, items);
        return VIEW_DENTAL_WORK;
    }

    @PostMapping("/{id}/edit")
    public String updateDentalWork(@PathVariable("id") Long id, @ModelAttribute DentalWork dentalWork, HttpSession session, Model model) {
        @SuppressWarnings("unchecked")
        List<ProductType> items = (List<ProductType>) session.getAttribute(ATTRIBUTE_KEY_MAP);
        if (items == null) {
            items = productMapService.findAll().getEntries();
            session.setAttribute(ATTRIBUTE_KEY_MAP, items);
        }
        model.addAttribute(ATTRIBUTE_KEY_MAP, items);
        dentalWork.setId(id);
        DentalWork updated = dentalWorkService.update(dentalWork);
        model.addAttribute("work", updated);
        return VIEW_DENTAL_WORK;
    }

    @PostMapping("/{id}/delete")
    public String deleteDentalWork(@PathVariable("id") Long id, HttpSession session) {
        dentalWorkService.delete(id);
        return MvcControllerUtil.REDIRECT + "dental-works";
    }
}

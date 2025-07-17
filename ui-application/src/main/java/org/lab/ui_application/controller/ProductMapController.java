package org.lab.ui_application.controller;

import jakarta.servlet.http.HttpSession;
import org.dental.restclient.DentalLabRestClient;
import org.dental.restclient.ProductMapService;
import org.lab.model.ProductType;
import org.lab.request.NewProductType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/main/product-map")
public class ProductMapController {

    private static final String ATTRIBUTE_KEY_MAP = "map";
    private static final String PRODUCT_MAP = "product-map";

    private final ProductMapService productMapService;


    @Autowired
    public ProductMapController(DentalLabRestClient dentalLabRestClient) {
        this.productMapService = dentalLabRestClient.PRODUCT_MAP;
    }


    @GetMapping
    public String productMapPage(HttpSession session, Model model) {
        @SuppressWarnings("unchecked")
        List<ProductType> items = (List<ProductType>) session.getAttribute(ATTRIBUTE_KEY_MAP);
        if (items == null) {
            items = productMapService.findAll().getEntries();
        }
        model.addAttribute(ATTRIBUTE_KEY_MAP, items);
        return PRODUCT_MAP;
    }

    @PostMapping
    public String addProduct(@RequestParam("title") String title,
                             @RequestParam("price") float price,
                             HttpSession session) {
        ProductType productType = productMapService.create(new NewProductType(title, price));
        @SuppressWarnings("unchecked")
        List<ProductType> items = (List<ProductType>) session.getAttribute(ATTRIBUTE_KEY_MAP);
        if (items != null) {
            items.add(productType);
            session.setAttribute(ATTRIBUTE_KEY_MAP, items);
        }
        return PRODUCT_MAP;
    }

    @PostMapping("/{id}/edit")
    public String editProduct(@PathVariable("id") UUID id,
                              @RequestParam("price") float price) {
        productMapService.updateProductType(id, price);
        return MvcControllerUtil.REDIRECT + "/main/product-map";
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable("id") UUID id) {
        productMapService.delete(id);
        return MvcControllerUtil.REDIRECT + "/main/product-map";
    }
}

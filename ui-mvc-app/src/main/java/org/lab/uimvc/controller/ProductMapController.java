package org.lab.uimvc.controller;

import org.dental.restclient.DentalLabRestClient;
import org.dental.restclient.ProductMapService;
import org.lab.model.ProductMap;
import org.lab.request.NewProductType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@Controller
@RequestMapping("/main/product-map")
public class ProductMapController extends MvcControllerUtil {

    private static final String PRODUCT_MAP = "product-map";
    private static final String PRODUCT_MAP_PATH = "/main/product-map";

    private final ProductMapService productMapService;


    @Autowired
    public ProductMapController(DentalLabRestClient dentalLabRestClient) {
        this.productMapService = dentalLabRestClient.PRODUCT_MAP;
    }


    @GetMapping
    public String productMapPage(Model model) {
        ProductMap map = productMapService.findAll();
        model.addAttribute(ATTRIBUTE_KEY_MAP, map.getEntries());
        return PRODUCT_MAP;
    }

    @PostMapping
    public String addProduct(@RequestParam("title") String title,
                             @RequestParam("price") float price) {
        productMapService.create(new NewProductType(title, price));
        return REDIRECT + PRODUCT_MAP_PATH;
    }

    @PostMapping("/{id}/edit")
    public String editProduct(@PathVariable("id") UUID id,
                              @RequestParam("price") float price) {
        productMapService.updateProductType(id, price);
        return REDIRECT + PRODUCT_MAP_PATH;
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable("id") UUID id) {
        productMapService.delete(id);
        return REDIRECT + PRODUCT_MAP_PATH;
    }
}

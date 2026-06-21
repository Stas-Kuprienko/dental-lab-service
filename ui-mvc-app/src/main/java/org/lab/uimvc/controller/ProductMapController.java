package org.lab.uimvc.controller;

import jakarta.servlet.http.HttpSession;
import org.lab.dental.feignclient.ProductMapService;
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
    public ProductMapController(ProductMapService productMapService) {
        this.productMapService = productMapService;
    }


    @GetMapping
    public String productMapPage(HttpSession session, Model model) {
        UUID userId = getUserId(session);
        ProductMap map = productMapService.findAll(userId);
        model.addAttribute(ATTRIBUTE_KEY_MAP, map.getEntries());
        return PRODUCT_MAP;
    }

    @PostMapping
    public String addProduct(@RequestParam("title") String title,
                             @RequestParam("price") float price,
                             HttpSession session, Model model) {
        UUID userId = getUserId(session);
        ProductMap map = productMapService.create(new NewProductType(title, price), userId);
        model.addAttribute(ATTRIBUTE_KEY_MAP, map.getEntries());
        return REDIRECT + PRODUCT_MAP_PATH;
    }

    @PostMapping("/{id}/edit")
    public String editProduct(@PathVariable("id") UUID id,
                              @RequestParam("price") float price,
                              HttpSession session, Model model) {
        UUID userId = getUserId(session);
        productMapService.update(id, price, userId);
        ProductMap map = productMapService.findAll(userId);
        model.addAttribute(ATTRIBUTE_KEY_MAP, map.getEntries());
        return REDIRECT + PRODUCT_MAP_PATH;
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable("id") UUID id,
                                HttpSession session, Model model) {
        UUID userId = getUserId(session);
        productMapService.delete(userId, id);
        ProductMap map = productMapService.findAll(userId);
        model.addAttribute(ATTRIBUTE_KEY_MAP, map.getEntries());
        return REDIRECT + PRODUCT_MAP_PATH;
    }
}

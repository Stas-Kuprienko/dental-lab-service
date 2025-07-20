package org.lab.uimvc.controller;

import jakarta.servlet.http.HttpSession;
import org.lab.model.ProductMap;
import org.lab.request.NewProductType;
import org.lab.uimvc.service.ProductMapMvcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@Controller
@RequestMapping("/main/product-map")
public class ProductMapController {

    private static final String PRODUCT_MAP = "product-map";
    private static final String PRODUCT_MAP_PATH = "/main/product-map";

    private final ProductMapMvcService productMapService;


    @Autowired
    public ProductMapController(ProductMapMvcService productMapService) {
        this.productMapService = productMapService;
    }


    @GetMapping
    public String productMapPage(HttpSession session, Model model) {
        UUID userId = (UUID) session.getAttribute(MvcControllerUtil.ATTRIBUTE_KEY_USER_ID);
        ProductMap map = productMapService.get(userId);
        model.addAttribute(MvcControllerUtil.ATTRIBUTE_KEY_MAP, map.getEntries());
        return PRODUCT_MAP;
    }

    @PostMapping
    public String addProduct(@RequestParam("title") String title,
                             @RequestParam("price") float price,
                             HttpSession session, Model model) {
        UUID userId = (UUID) session.getAttribute(MvcControllerUtil.ATTRIBUTE_KEY_USER_ID);
        ProductMap map = productMapService.create(userId, new NewProductType(title, price));
        model.addAttribute(MvcControllerUtil.ATTRIBUTE_KEY_MAP, map.getEntries());
        return MvcControllerUtil.REDIRECT + PRODUCT_MAP_PATH;
    }

    @PostMapping("/{id}/edit")
    public String editProduct(@PathVariable("id") UUID id,
                              @RequestParam("price") float price,
                              HttpSession session, Model model) {
        UUID userId = (UUID) session.getAttribute(MvcControllerUtil.ATTRIBUTE_KEY_USER_ID);
        ProductMap map = productMapService.update(userId, id, price);
        model.addAttribute(MvcControllerUtil.ATTRIBUTE_KEY_MAP, map.getEntries());
        return MvcControllerUtil.REDIRECT + PRODUCT_MAP_PATH;
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable("id") UUID id,
                                HttpSession session, Model model) {
        UUID userId = (UUID) session.getAttribute(MvcControllerUtil.ATTRIBUTE_KEY_USER_ID);
        ProductMap map = productMapService.delete(userId, id);
        model.addAttribute(MvcControllerUtil.ATTRIBUTE_KEY_MAP, map.getEntries());
        return MvcControllerUtil.REDIRECT + PRODUCT_MAP_PATH;
    }
}

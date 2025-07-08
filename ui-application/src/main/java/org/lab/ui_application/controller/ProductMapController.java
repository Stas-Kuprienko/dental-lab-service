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
public class ProductMapController extends MvcControllerUtil {

    private final ProductMapService productMapClient;

    @Autowired
    public ProductMapController(DentalLabRestClient dentalLabRestClient) {
        this.productMapClient = dentalLabRestClient.PRODUCT_MAP;
    }

    @GetMapping
    public String productMapPage(HttpSession session, Model model) {
        UUID userId = UUID.fromString("30ac0d36-cd43-4083-9494-f2b37b12dc9c");
        @SuppressWarnings("unchecked")
        List<ProductType> items = (List<ProductType>) session.getAttribute(ATTRIBUTE_KEY_MAP);
        if (items == null) {
            items = productMapClient.findAll(userId).getEntries();
        }
        session.setAttribute("userId", userId);
        model.addAttribute(ATTRIBUTE_KEY_MAP, items);
        return "product-map";
    }

    @PostMapping
    public String addProduct(@RequestParam("title") String title,
                             @RequestParam("price") float price,
                             HttpSession session) {
        UUID userId = (UUID) session.getAttribute("userId");
        ProductType productType = productMapClient.create(userId, new NewProductType(title, price));
        @SuppressWarnings("unchecked")
        List<ProductType> items = (List<ProductType>) session.getAttribute(ATTRIBUTE_KEY_MAP);
        if (items != null) {
            items.add(productType);
            session.setAttribute(ATTRIBUTE_KEY_MAP, items);
        }
        return REDIRECT + "/main/product-map";
    }

    @PostMapping("/{id}/edit")
    public String editProduct(@PathVariable("id") UUID id,
                              @RequestParam("price") float price,
                              HttpSession session) {
        UUID userId = (UUID) session.getAttribute("userId");
        productMapClient.updateProductType(userId, id, price);
        return REDIRECT + "/main/product-map";
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable("id") UUID id,
                                HttpSession session) {
        UUID userId = (UUID) session.getAttribute("userId");
        productMapClient.delete(userId, id);
        return REDIRECT + "/main/product-map";
    }
}

package org.lab.ui_application.controller;

import jakarta.servlet.http.HttpSession;
import org.dental.restclient.DentalLabRestClient;
import org.dental.restclient.ProductMapService;
import org.lab.model.ProductType;
import org.lab.request.NewProductType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/main/product-map")
public class ProductMapController {

    private final String apiUrl;
    private final ProductMapService productMapClient;

    @Autowired
    public ProductMapController(@Value("${project.variables.dental-lab-url}") String apiUrl,
                                DentalLabRestClient dentalLabRestClient) {
        this.apiUrl = apiUrl;
        this.productMapClient = dentalLabRestClient.PRODUCT_MAP;
    }

    @GetMapping
    public String productMapPage(HttpSession session, Model model) {
        UUID userId = UUID.fromString("30ac0d36-cd43-4083-9494-f2b37b12dc9c");
        List<ProductType> items = productMapClient.findAll(userId).getEntries();
        session.setAttribute("userId", userId);
        model.addAttribute("backendUrl", apiUrl);
        model.addAttribute("map", items);
        return "product-map";
    }

    @PostMapping
    public String addProduct(@RequestParam String title,
                             @RequestParam float price,
                             HttpSession session) {
        UUID userId = (UUID) session.getAttribute("userId");
        productMapClient.create(userId, new NewProductType(title, price));
        return "redirect:/main/product-map";
    }

    @PostMapping("/edit")
    public String editProduct(@RequestParam UUID id,
                              @RequestParam float price,
                              HttpSession session) {
        UUID userId = (UUID) session.getAttribute("userId");
        productMapClient.updateProductType(userId, id, price);
        return "redirect:/main/product-map";
    }

    @PostMapping("/delete")
    public String deleteProduct(@RequestParam UUID id,
                                HttpSession session) {
        UUID userId = (UUID) session.getAttribute("userId");
        productMapClient.delete(userId, id);
        return "redirect:/main/product-map";
    }
}

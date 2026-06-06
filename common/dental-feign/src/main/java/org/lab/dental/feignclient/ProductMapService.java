package org.lab.dental.feignclient;

import org.lab.model.ProductMap;
import org.lab.model.ProductType;
import org.lab.request.NewProductType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@FeignClient(url = "${project.variables.dental-lab-api.url}", path = "/product_map")
public interface ProductMapService {


    @PostMapping
    ProductType create(@RequestBody NewProductType newProductType);

    @GetMapping("/{id}")
    ProductType findById(@PathVariable("id") UUID id);

    @GetMapping
    ProductMap findAll();

    @PutMapping("/{id}")
    void updateProductType(@PathVariable("id") UUID id, @RequestBody float newPrice);

    @DeleteMapping("/{id}")
    void deleteProductType(@PathVariable("id") UUID id);
}

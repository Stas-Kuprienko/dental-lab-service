package org.lab.dental.feignclient;

import org.lab.model.ProductMap;
import org.lab.model.ProductType;
import org.lab.request.NewProductType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@FeignClient(url = "${project.variables.dental-lab-api.url}", path = "/product_map", name = "product-map-service")
public interface ProductMapService {


    @PostMapping
    ProductMap create(@RequestBody NewProductType newProductType);

    @PostMapping
    ProductMap create(@RequestBody NewProductType newProductType, @RequestHeader("X-USER-ID") UUID userId);

    @GetMapping("/{id}")
    ProductType findById(@PathVariable("id") UUID id);

    @GetMapping("/{id}")
    ProductType findById(@PathVariable("id") UUID id, @RequestHeader("X-USER-ID") UUID userId);

    @GetMapping
    ProductMap findAll();

    @GetMapping
    ProductMap findAll(@RequestHeader("X-USER-ID") UUID userId);

    @PutMapping("/{id}")
    void update(@PathVariable("id") UUID id, @RequestBody float newPrice);

    @PutMapping("/{id}")
    void update(@PathVariable("id") UUID id, @RequestBody float newPrice, @RequestHeader("X-USER-ID") UUID userId);

    @DeleteMapping("/{id}")
    void delete(@PathVariable("id") UUID id);

    @DeleteMapping("/{id}")
    void delete(@PathVariable("id") UUID id, @RequestHeader("X-USER-ID") UUID userId);
}

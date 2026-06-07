package org.lab.dental.feignclient;

import org.lab.model.DentalWork;
import org.lab.request.NewProduct;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.UUID;

@FeignClient(url = "${project.variables.dental-lab-api.url}", path = "/dental_works", name = "product-service")
public interface ProductService {


    @PostMapping("/{work_id}/products")
    DentalWork addProduct(@PathVariable("work_id") long workId, @RequestBody NewProduct newProduct);

    @PostMapping("/{work_id}/products")
    DentalWork addProduct(@PathVariable("work_id") long workId,
                          @RequestBody NewProduct newProduct,
                          @RequestHeader("X-USER-ID") UUID userId);

    @PutMapping("/{work_id}/products/{id}")
    DentalWork updateCompletion(@PathVariable("work_id") long workId, @PathVariable("id") UUID id, @RequestBody LocalDate completeAt);

    @PutMapping("/{work_id}/products/{id}")
    DentalWork updateCompletion(@PathVariable("work_id") long workId,
                                @PathVariable("id") UUID id,
                                @RequestBody LocalDate completeAt,
                                @RequestHeader("X-USER-ID") UUID userId);

    @DeleteMapping("/{work_id}/products/{id}")
    DentalWork deleteProduct(@PathVariable("work_id") long workId, @PathVariable("id") UUID id);

    @DeleteMapping("/{work_id}/products/{id}")
    DentalWork deleteProduct(@PathVariable("work_id") long workId,
                             @PathVariable("id") UUID id,
                             @RequestHeader("X-USER-ID") UUID userId);
}

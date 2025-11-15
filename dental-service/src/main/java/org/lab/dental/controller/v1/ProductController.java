package org.lab.dental.controller.v1;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.lab.dental.service.DentalWorkManager;
import org.lab.model.DentalWork;
import org.lab.request.NewProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/dental_works/{work_id}/products")
public class ProductController {

    private final DentalWorkManager dentalWorkManager;

    @Autowired
    public ProductController(DentalWorkManager dentalWorkManager) {
        this.dentalWorkManager = dentalWorkManager;
    }


    @PostMapping
    public ResponseEntity<DentalWork> addProduct(@RequestHeader("X-USER-ID") UUID userId,
                                                 @PathVariable("work_id") Long workId,
                                                 @RequestBody @Valid NewProduct newProduct) {

        log.info("From user '{}' received request to add: {}", userId, newProduct);
        DentalWork dentalWork = dentalWorkManager.addProduct(workId, userId, newProduct);
        return ResponseEntity.ok(dentalWork);
    }


    @PutMapping("/{id}")
    public ResponseEntity<DentalWork> updateCompletion(@RequestHeader("X-USER-ID") UUID userId,
                                                       @PathVariable("work_id") Long workId,
                                                       @PathVariable("id") UUID productId,
                                                       @RequestBody LocalDate completeAt) {
        log.info("From user '{}' received request to update completion ({}) by ID='{}' and workID={}", userId, completeAt, productId, workId);
        DentalWork dentalWork = dentalWorkManager.updateProductCompletion(workId, userId, productId, completeAt);
        return ResponseEntity.ok(dentalWork);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<DentalWork> deleteProduct(@RequestHeader("X-USER-ID") UUID userId,
                                                    @PathVariable("work_id") Long workId,
                                                    @PathVariable("id") UUID id) {

        log.info("From user '{}' received request to delete by ID='{}' and workID={}", userId, id, workId);
        DentalWork dentalWork = dentalWorkManager.deleteProduct(workId, userId, id);
        return ResponseEntity.ok(dentalWork);
    }
}

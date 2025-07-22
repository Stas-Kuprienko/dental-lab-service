package org.lab.dental.controller.v1;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.lab.dental.entity.DentalWorkEntity;
import org.lab.dental.mapping.DentalWorkConverter;
import org.lab.dental.service.DentalWorkService;
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

    private final DentalWorkService dentalWorkService;
    private final DentalWorkConverter dentalWorkConverter;

    @Autowired
    public ProductController(DentalWorkService dentalWorkService,
                             DentalWorkConverter dentalWorkConverter) {
        this.dentalWorkService = dentalWorkService;
        this.dentalWorkConverter = dentalWorkConverter;
    }


    @PostMapping
    public ResponseEntity<DentalWork> addProduct(@RequestHeader("X-USER-ID") UUID userId,
                                                 @PathVariable("work_id") Long workId,
                                                 @RequestBody @Valid NewProduct newProduct) {

        log.info("From user '{}' received request to add: {}", userId, newProduct);
        UUID productTypeId = newProduct.getProduct();
        Integer quantity = newProduct.getQuantity();
        LocalDate completeAt = newProduct.getCompleteAt();
        DentalWorkEntity dentalWork = dentalWorkService.addProduct(workId, userId, productTypeId, quantity, completeAt);
        return ResponseEntity.ok(dentalWorkConverter.toDto(dentalWork));
    }


    @PutMapping("/{id}")
    public ResponseEntity<DentalWork> updateCompletion(@RequestHeader("X-USER-ID") UUID userId,
                                                       @PathVariable("work_id") Long workId,
                                                       @PathVariable("id") UUID productId,
                                                       @RequestBody LocalDate completeAt) {
        log.info("From user '{}' received request to update completion ({}) by ID='{}' and workID={}", userId, completeAt, productId, workId);
        DentalWorkEntity entity = dentalWorkService.updateProductCompletion(workId, userId, productId, completeAt);
        return ResponseEntity.ok(dentalWorkConverter.toDto(entity));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<DentalWork> deleteProduct(@RequestHeader("X-USER-ID") UUID userId,
                                                    @PathVariable("work_id") Long workId,
                                                    @PathVariable("id") UUID id) {

        log.info("From user '{}' received request to delete by ID='{}' and workID={}", userId, id, workId);
        DentalWorkEntity entity = dentalWorkService.deleteProduct(workId, userId, id);
        return ResponseEntity.ok(dentalWorkConverter.toDto(entity));
    }
}

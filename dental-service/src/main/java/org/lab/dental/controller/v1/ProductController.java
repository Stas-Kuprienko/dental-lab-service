package org.lab.dental.controller.v1;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.lab.dental.entity.DentalWorkEntity;
import org.lab.dental.entity.ProductEntity;
import org.lab.dental.mapping.DentalWorkConverter;
import org.lab.dental.mapping.ProductConverter;
import org.lab.dental.service.DentalWorkService;
import org.lab.dto.DentalWork;
import org.lab.dto.Product;
import org.lab.request.NewProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/dental_works/{work_id}/products")
public class ProductController {

    private final DentalWorkService dentalWorkService;
    private final DentalWorkConverter dentalWorkConverter;
    private final ProductConverter productConverter;

    @Autowired
    public ProductController(DentalWorkService dentalWorkService,
                             DentalWorkConverter dentalWorkConverter,
                             ProductConverter productConverter) {
        this.dentalWorkService = dentalWorkService;
        this.dentalWorkConverter = dentalWorkConverter;
        this.productConverter = productConverter;
    }


    @PostMapping
    public ResponseEntity<DentalWork> addProduct(@RequestHeader("X-USER-ID") UUID userId,
                                                 @PathVariable("work_id") Long workId,
                                                 @RequestBody @Valid NewProduct newProduct) {

        log.info("From user '{}' received request to add: {}", userId, newProduct);
        ProductEntity product = productConverter.fromRequest(newProduct);
        DentalWorkEntity dentalWork = dentalWorkService.addProduct(workId, userId, product);
        return ResponseEntity.ok(dentalWorkConverter.toDto(dentalWork));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<DentalWork> deleteProduct(@RequestHeader("X-USER-ID") UUID userId,
                                                    @PathVariable("work_id") Long workId,
                                                    @PathVariable("id") UUID id) {

        log.info("From user '{}' received request to delete by ID='{}' and workID={}", userId, id, workId);
        dentalWorkService.deleteProduct(workId, userId, id);
        DentalWorkEntity entity = dentalWorkService.getByIdAndUserId(workId, userId);
        return ResponseEntity.ok(dentalWorkConverter.toDto(entity));
    }
}

package org.lab.dental.controller.v1;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.lab.dental.service.ProductTypeService;
import org.lab.dental.util.RequestMappingReader;
import org.lab.model.ProductMap;
import org.lab.model.ProductType;
import org.lab.request.NewProductType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.UUID;

@Slf4j
@Tag(name = "Product Map")
@RestController
@RequestMapping("/api/v1/product_map")
public class ProductMapController {

    private final ProductTypeService productTypeService;
    private final String URL;


    @Autowired
    public ProductMapController(ProductTypeService productTypeService) {
        this.productTypeService = productTypeService;
        URL = RequestMappingReader.read(this.getClass());
    }


    @PostMapping
    public ResponseEntity<ProductMap> create(@RequestAttribute("X-USER-ID") UUID userId,
                                             @RequestBody @Valid NewProductType newProductType) {
        log.info("From user '{}' received request to create new product type: {}", userId, newProductType);
        ProductMap productMap = productTypeService.create(newProductType, userId);
        return ResponseEntity
                .created(URI.create(URL)).body(productMap);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductType> findById(@RequestAttribute("X-USER-ID") UUID userId,
                                                @PathVariable("id") UUID id) {
        log.info("From user '{}' received request to find product type by id={}", userId, id);
        ProductType productType = productTypeService.getByIdAndUserId(id, userId);
        return ResponseEntity.ok(productType);
    }

    @GetMapping
    public ResponseEntity<ProductMap> findAll(@RequestAttribute("X-USER-ID") UUID userId) {
        log.info("From user '{}' received request to get product map", userId);
        ProductMap productMap = productTypeService.getAllByUserId(userId);
        return ResponseEntity.ok(productMap);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateProductType(@RequestAttribute("X-USER-ID") UUID userId,
                                                  @PathVariable("id") UUID id,
                                                  @RequestBody Float newPrice) {
        log.info("From user '{}' received request to update productType ID={}", userId, id);
        productTypeService.update(id, userId, newPrice);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductType(@RequestAttribute("X-USER-ID") UUID userId,
                                                  @PathVariable("id") UUID id) {
        log.info("From user '{}' received request to delete by ID={}", userId, id);
        productTypeService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}

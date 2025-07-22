package org.lab.dental.controller.v1;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.lab.dental.entity.ProductTypeEntity;
import org.lab.dental.mapping.ProductTypeConverter;
import org.lab.dental.service.ProductTypeService;
import org.lab.dental.util.RequestMappingReader;
import org.lab.model.ProductMap;
import org.lab.model.ProductType;
import org.lab.request.NewProductType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/product_map")
public class ProductMapController {

    private final String URL;

    private final ProductTypeService productTypeService;
    private final ProductTypeConverter converter;

    @Autowired
    public ProductMapController(ProductTypeService productTypeService, ProductTypeConverter converter) {
        this.productTypeService = productTypeService;
        this.converter = converter;
        URL = RequestMappingReader.read(this.getClass());
    }


    @PostMapping
    public ResponseEntity<ProductType> create(@RequestHeader("X-USER-ID") UUID userId,
                                              @RequestBody @Valid NewProductType newProductType) {

        log.info("From user '{}' received request: {}", userId, newProductType);
        ProductTypeEntity entity = converter.fromRequest(newProductType, userId);
        entity = productTypeService.create(entity);
        return ResponseEntity
                .created(URI.create(URL + '/' + entity.getId())).body(converter.toDto(entity));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ProductType> findById(@RequestHeader("X-USER-ID") UUID userId,
                                                @PathVariable("id") UUID id) {

        log.info("From user '{}' received request with parameter: id={}", userId, id);
        ProductTypeEntity entity = productTypeService.getByIdAndUserId(id, userId);
        return ResponseEntity.ok(converter.toDto(entity));
    }


    @GetMapping
    public ResponseEntity<ProductMap> findAll(@RequestHeader("X-USER-ID") UUID userId) {

        log.info("From user '{}' received request", userId);
        List<ProductTypeEntity> entities = productTypeService.getAllByUserId(userId);
        ProductMap productMap = converter.toProductMap(userId, entities);
        return ResponseEntity.ok(productMap);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Void> updateProductType(@RequestHeader("X-USER-ID") UUID userId,
                                                  @PathVariable("id") UUID id,
                                                  @RequestBody Float newPrice) {

        log.info("From user '{}' received request to update productType ID={}", userId, id);
        productTypeService.update(id, userId, newPrice);
        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductType(@RequestHeader("X-USER-ID") UUID userId,
                                                  @PathVariable("id") UUID id) {

        log.info("From user '{}' received request to delete by ID={}", userId, id);
        productTypeService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}

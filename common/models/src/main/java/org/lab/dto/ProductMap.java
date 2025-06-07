package org.lab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class ProductMap {

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("entries")
    private List<ProductType> entries;


    public ProductMap(UUID userId, List<ProductType> entries) {
        this.userId = userId;
        this.entries = entries;
    }

    public ProductMap() {}
}

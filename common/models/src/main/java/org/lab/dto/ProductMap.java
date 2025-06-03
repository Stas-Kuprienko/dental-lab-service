package org.lab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class ProductMap {

    @JsonProperty("entries")
    private List<ProductType> entries;

    public ProductMap(List<ProductType> entries) {
        this.entries = entries;
    }
}

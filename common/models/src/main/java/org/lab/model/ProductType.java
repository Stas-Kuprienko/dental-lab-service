package org.lab.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class ProductType implements Serializable {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("price")
    private float price;


    public ProductType() {}
}

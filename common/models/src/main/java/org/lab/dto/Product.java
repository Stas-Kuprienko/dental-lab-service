package org.lab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class Product {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("product_type")
    private String productType;

    @JsonProperty("price")
    private double price;

    @JsonProperty("quantity")
    private int quantity;

    @JsonProperty("dental_work_id")
    private long dentalWorkId;
}

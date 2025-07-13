package org.lab.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class Product {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("price")
    private float price;

    @JsonProperty("quantity")
    private int quantity;

    @JsonProperty("dental_work_id")
    private long dentalWorkId;

    @JsonProperty("accepted_at")
    private LocalDate acceptedAt;

    @JsonProperty("complete_at")
    private LocalDate completeAt;


    public Product() {}
}

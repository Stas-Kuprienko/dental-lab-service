package org.lab.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class NewProduct {

    @JsonProperty("title")
    private String title;

    @JsonProperty("price")
    private float price;

    @JsonProperty("quantity")
    private int quantity;

    @JsonProperty("dental_work_id")
    private long dentalWorkId;


    public NewProduct() {}
}

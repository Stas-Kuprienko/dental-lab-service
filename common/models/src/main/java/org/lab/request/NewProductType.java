package org.lab.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class NewProductType {

    @JsonProperty("title")
    private String title;

    @JsonProperty("price")
    private float price;


    public NewProductType() {}
}

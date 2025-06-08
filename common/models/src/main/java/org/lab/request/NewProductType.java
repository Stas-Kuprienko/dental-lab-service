package org.lab.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class NewProductType {

    @NotBlank
    @JsonProperty("title")
    private String title;

    @Positive
    @JsonProperty("price")
    private float price;


    public NewProductType() {}
}

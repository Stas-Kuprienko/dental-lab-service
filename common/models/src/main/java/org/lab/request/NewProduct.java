package org.lab.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class NewProduct {

    @NotNull
    @JsonProperty("product")
    private UUID product;

    @Positive
    @JsonProperty("quantity")
    private int quantity;


    public NewProduct() {}
}

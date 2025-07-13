package org.lab.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
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

    @NotNull
    @JsonProperty("complete_at")
    private LocalDate completeAt;


    public NewProduct() {}
}

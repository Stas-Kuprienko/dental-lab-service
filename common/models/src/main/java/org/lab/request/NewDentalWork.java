package org.lab.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class NewDentalWork {

    @NotBlank
    @JsonProperty("clinic")
    private String clinic;

    @NotBlank
    @JsonProperty("patient")
    private String patient;

    @JsonProperty("complete_at")
    @Schema(pattern = "YYYY-MM-DD")
    private LocalDate completeAt;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("product_id")
    private UUID productId;

    @JsonProperty("quantity")
    private Integer quantity;


    public NewDentalWork() {}
}

package org.lab.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.lab.enums.WorkStatus;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class DentalWork implements Serializable {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("clinic")
    private String clinic;

    @JsonProperty("patient")
    private String patient;

    @JsonProperty("accepted_at")
    private LocalDate acceptedAt;

    @JsonProperty("complete_at")
    private LocalDate completeAt;

    @JsonProperty("status")
    private WorkStatus status;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("products")
    private List<Product> products;

    @JsonProperty("photo_links")
    private List<String> photoLinks;


    public DentalWork() {}


    public Integer quantityFor(String productKey) {
        return products.stream()
                .filter(p -> p.getTitle().equals(productKey))
                .map(Product::getQuantity)
                .findFirst()
                .orElse(null);
    }

    public int countPhoto() {
        if (photoLinks == null) {
            return 0;
        } else {
            return photoLinks.size();
        }
    }
}

package org.lab.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.*;

@Data
public class ProductMap implements Serializable {

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("entries")
    private List<ProductType> entries;


    public ProductMap(UUID userId, List<ProductType> entries) {
        this.userId = userId;
        this.entries = entries;
    }

    public ProductMap() {}


    public List<ProductType> sortEntries() {
        if (entries == null) {
            return new ArrayList<>();
        }
        entries.sort(Comparator.comparing(ProductType::getTitle));
        return entries;
    }

    public Optional<ProductType> get(UUID id) {
        if (entries == null) {
            return Optional.empty();
        }
        return entries
                .stream()
                .filter(productType -> productType.getId().equals(id))
                .findFirst();
    }

    public boolean delete(UUID id) {
        if (entries == null) {
            return false;
        }
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getId().equals(id)) {
                entries.remove(i);
                return true;
            }
        }
        return false;
    }
}

package org.lab.old;

import org.lab.model.Product;
import java.time.LocalDate;

public class OldProductConverter {

    public Product parse(OldProduct product, LocalDate acceptedAt, LocalDate completeAt) {
        char[] chars = product.getTitle().toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        String title = String.valueOf(chars);
        return Product.builder()
                .title(title)
                .quantity(product.getQuantity())
                .price(product.getPrice())
                .acceptedAt(acceptedAt)
                .completeAt(completeAt)
                .build();
    }
}

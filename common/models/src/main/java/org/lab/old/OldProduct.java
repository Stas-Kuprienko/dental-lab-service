package org.lab.old;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class OldProduct {

    private int entryId;

    private String title;

    private byte quantity;

    private int price;


    public OldProduct() {}
}
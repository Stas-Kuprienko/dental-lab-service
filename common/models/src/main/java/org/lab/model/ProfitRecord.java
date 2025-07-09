package org.lab.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class ProfitRecord {

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("value")
    private double value;

    @JsonProperty("year")
    private int year;

    @JsonProperty("month")
    private Month month;


    public ProfitRecord() {}


    public int monthValue() {
        return month.getValue();
    }

    public String monthToString() {
        return month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.of("ru")).toUpperCase();
    }
}

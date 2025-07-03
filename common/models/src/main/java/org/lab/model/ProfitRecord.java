package org.lab.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.YearMonth;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class ProfitRecord {

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("value")
    private double value;

    @JsonProperty("year_month")
    private YearMonth yearMonth;


    public ProfitRecord() {}
}

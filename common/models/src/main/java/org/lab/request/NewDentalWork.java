package org.lab.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class NewDentalWork {

    @JsonProperty("clinic")
    private String clinic;

    @JsonProperty("patient")
    private String patient;

    @JsonProperty("complete_at")
    private LocalDate completeAt;

    @JsonProperty("comment")
    private String comment;


    public NewDentalWork() {}
}

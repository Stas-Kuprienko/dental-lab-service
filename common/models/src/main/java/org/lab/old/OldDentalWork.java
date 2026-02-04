package org.lab.old;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class OldDentalWork {

    private int id;

    private int userId;

    private String patient;

    private String clinic;

    private List<OldProduct> products;

    private LocalDate accepted;

    private LocalDate complete;

    private Status status;

    private String comment;

    private int reportId;


    public OldDentalWork() {}


    public enum Status {
        MAKE, CLOSED, PAID
    }
}

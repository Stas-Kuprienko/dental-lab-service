package org.lab.old;

import org.lab.enums.WorkStatus;
import org.lab.model.DentalWork;
import org.lab.model.Product;

import java.util.List;
import java.util.UUID;

public class OldDentalWorkConverter {

    private final OldProductConverter oldProductConverter;

    public OldDentalWorkConverter(OldProductConverter oldProductConverter) {
        this.oldProductConverter = oldProductConverter;
    }


    public DentalWork parse(OldDentalWork dentalWork, UUID userId) {
        WorkStatus status = switch (dentalWork.getStatus()) {
            case MAKE -> WorkStatus.MAKING;
            case CLOSED -> WorkStatus.COMPLETED;
            case PAID -> WorkStatus.PAID;
        };
        List<Product> products = dentalWork
                .getProducts()
                .stream()
                .map(op -> oldProductConverter.parse(op, dentalWork.getAccepted(), dentalWork.getComplete()))
                .toList();
        return DentalWork.builder()
                .userId(userId)
                .acceptedAt(dentalWork.getAccepted())
                .clinic(dentalWork.getClinic())
                .patient(dentalWork.getPatient())
                .completeAt(dentalWork.getComplete())
                .status(status)
                .comment(dentalWork.getComment())
                .products(products)
                .build();
    }
}

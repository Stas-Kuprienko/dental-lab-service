package org.lab.dental.mapping;

import org.lab.dental.entity.DentalWorkEntity;
import org.lab.model.DentalWork;
import org.lab.request.NewDentalWork;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;

@Component
public class DentalWorkConverter {

    private final ProductConverter productConverter;

    @Autowired
    public DentalWorkConverter(ProductConverter productConverter) {
        this.productConverter = productConverter;
    }


    public DentalWorkEntity toEntity(DentalWork dto) {
        return DentalWorkEntity.builder()
                .id(dto.getId())
                .userId(dto.getUserId())
                .clinic(dto.getClinic())
                .patient(dto.getPatient())
                .status(dto.getStatus())
                .acceptedAt(dto.getAcceptedAt())
                .completeAt(dto.getCompleteAt())
                .comment(dto.getComment())
                .products(dto.getProducts() == null ? List.of() : dto.getProducts().stream().map(productConverter::toEntity).toList())
                .build();
    }

    public DentalWork toDto(DentalWorkEntity entity) {
        return DentalWork.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .clinic(entity.getClinic())
                .patient(entity.getPatient())
                .status(entity.getStatus())
                .acceptedAt(entity.getAcceptedAt())
                .completeAt(entity.getCompleteAt())
                .comment(entity.getComment())
                .products(entity.getProducts() == null ? List.of() : entity.getProducts().stream().map(productConverter::toDto).toList())
                .build();
    }

    public DentalWorkEntity fromRequest(NewDentalWork newDentalWork, UUID userId) {
        return DentalWorkEntity.builder()
                .clinic(newDentalWork.getClinic())
                .patient(newDentalWork.getPatient())
                .completeAt(newDentalWork.getCompleteAt())
                .comment(newDentalWork.getComment())
                .userId(userId)
                .build();
    }
}

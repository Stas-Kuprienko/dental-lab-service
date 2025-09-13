package org.lab.telegram_bot.service;

import org.lab.model.DentalWork;
import org.lab.request.NewDentalWork;
import org.lab.request.NewProduct;
import org.lab.telegram_bot.datasource.DentalWorkRepository;
import org.lab.telegram_bot.utils.DentalWorkList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DentalWorkMvcService {

    private final DentalWorkServiceWrapper dentalWorkService;
    private final ProductServiceWrapper productService;
    private final DentalWorkRepository dentalWorkRepository;


    @Autowired
    public DentalWorkMvcService(DentalLabRestClientWrapper dentalLabRestClient, DentalWorkRepository dentalWorkRepository) {
        this.dentalWorkService = dentalLabRestClient.DENTAL_WORKS;
        this.productService = dentalLabRestClient.PRODUCTS;
        this.dentalWorkRepository = dentalWorkRepository;
    }


    public List<DentalWork> createAndReturnList(NewDentalWork newDentalWork, UUID userId) {
        DentalWork dentalWork = dentalWorkService.create(newDentalWork, userId);
        DentalWorkList dentalWorks = getDentalWorkList(userId);
        dentalWorks.add(dentalWork);
        dentalWorkRepository.save(dentalWorks);
        return dentalWorks.toList();
    }

    public DentalWork createAndReturnSingle(NewDentalWork newDentalWork, UUID userId) {
        DentalWork dentalWork = dentalWorkService.create(newDentalWork, userId);
        if (isCurrentMonth(dentalWork)) {
            DentalWorkList dentalWorks = getDentalWorkList(userId);
            dentalWorks.add(dentalWork);
            dentalWorkRepository.save(dentalWorks);
        }
        return dentalWork;
    }

    public DentalWork getById(long id, UUID userId) {
        DentalWorkList dentalWorks = getDentalWorkList(userId);
        return dentalWorks.findById(id).orElse(dentalWorkService.findById(id, userId));
    }

    public List<DentalWork> getAll(UUID userId) {
        Optional<DentalWorkList> optionalDentalWorks = dentalWorkRepository.getAll(userId);
        if (optionalDentalWorks.isEmpty()) {
            List<DentalWork> dentalWorks = dentalWorkService.findAll(userId);
            DentalWorkList dentalWorkList = DentalWorkList.create(dentalWorks, userId);
            dentalWorkRepository.save(dentalWorkList);
            return dentalWorkList.toList();
        } else {
            return optionalDentalWorks.get().toList();
        }
    }

    public DentalWork addProduct(long workId, UUID productTypeId, int quantity, LocalDate completeAt, UUID userId) {
        NewProduct newProduct = NewProduct.builder()
                .product(productTypeId)
                .quantity(quantity)
                .completeAt(completeAt)
                .build();
        DentalWork dentalWork = productService.addProduct(workId, newProduct, userId);
        if (isCurrentMonth(dentalWork)) {
            DentalWorkList dentalWorks = getDentalWorkList(userId);
            dentalWorks.add(dentalWork);
            dentalWorkRepository.save(dentalWorks);
        }
        return dentalWork;
    }


    private DentalWorkList getDentalWorkList(UUID userId) {
        Optional<DentalWorkList> optionalDentalWorkList = dentalWorkRepository.getAll(userId);
        return optionalDentalWorkList.orElseGet(() -> DentalWorkList.create(dentalWorkService.findAll(userId), userId));
    }

    private boolean isCurrentMonth(DentalWork dentalWork) {
        return dentalWork.getCompleteAt().getMonth().equals(LocalDate.now().getMonth());
    }
}

package org.lab.telegram_bot.service;

import org.lab.model.DentalWork;
import org.lab.request.NewDentalWork;
import org.lab.telegram_bot.datasource.DentalWorkRepository;
import org.lab.telegram_bot.utils.DentalWorkList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DentalWorkMvcService {

    private final DentalWorkServiceWrapper dentalWorkService;
    private final DentalWorkRepository dentalWorkRepository;

    @Autowired
    public DentalWorkMvcService(DentalLabRestClientWrapper dentalLabRestClient, DentalWorkRepository dentalWorkRepository) {
        this.dentalWorkService = dentalLabRestClient.DENTAL_WORKS;
        this.dentalWorkRepository = dentalWorkRepository;
    }


    public List<DentalWork> createAndReturnList(NewDentalWork newDentalWork, UUID userId) {
        DentalWork dentalWork = dentalWorkService.create(newDentalWork, userId);
        DentalWorkList dentalWorks = getDentalWorkList(userId);
        dentalWorks.add(dentalWork);
        dentalWorkRepository.save(dentalWorks);
        return dentalWorks.getDentalWorks();
    }

    public DentalWork createAndReturnSingle(NewDentalWork newDentalWork, UUID userId) {
        DentalWork dentalWork = dentalWorkService.create(newDentalWork, userId);
        DentalWorkList dentalWorks = getDentalWorkList(userId);
        dentalWorks.add(dentalWork);
        dentalWorkRepository.save(dentalWorks);
        return dentalWork;
    }

    public DentalWork getById(long id, UUID userId) {
        DentalWorkList dentalWorks = getDentalWorkList(userId);
        return dentalWorks.getById(id).orElse(dentalWorkService.findById(id, userId));
    }

    public List<DentalWork> getAll(UUID userId) {
        Optional<DentalWorkList> optionalDentalWorks = dentalWorkRepository.getAll(userId);
        if (optionalDentalWorks.isEmpty()) {
            DentalWorkList dentalWorks = new DentalWorkList(dentalWorkService.findAll(userId), userId);
            dentalWorkRepository.save(dentalWorks);
            return dentalWorks.getDentalWorks();
        } else {
            return optionalDentalWorks.get().getDentalWorks();
        }
    }


    private DentalWorkList getDentalWorkList(UUID userId) {
        return dentalWorkRepository.getAll(userId).orElse(new DentalWorkList(dentalWorkService.findAll(userId), userId));
    }
}

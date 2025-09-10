package org.lab.telegram_bot.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.lab.model.DentalWork;
import org.springframework.data.redis.core.RedisHash;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Data
@AllArgsConstructor
@RedisHash("dentalWorkList")
public class DentalWorkList implements Serializable {

    private List<DentalWork> dentalWorks;

    private UUID userId;


    public DentalWorkList() {}


    public void add(DentalWork dentalWork) {
        if (dentalWorks == null) {
            dentalWorks = new ArrayList<>();
        }
        dentalWorks.add(dentalWork);
    }

    public Optional<DentalWork> getById(long id) {
        if (dentalWorks == null || dentalWorks.isEmpty()) {
            return Optional.empty();
        } else {
            return dentalWorks
                    .stream()
                    .filter(dw -> dw.getId().equals(id))
                    .findFirst();
        }
    }

    public int size() {
        if (dentalWorks == null) {
            dentalWorks = new ArrayList<>();
        }
        return dentalWorks.size();
    }
}

package org.lab.dental.repository.redis;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.lab.model.DentalWork;
import org.springframework.data.redis.core.RedisHash;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Data
@AllArgsConstructor
@RedisHash("dentalWorkList")
public class DentalWorkList implements Serializable {

    private HashMap<Long, DentalWork> dentalWorks;

    private UUID userId;


    public DentalWorkList() {
        this.dentalWorks = new HashMap<>();
    }


    public static DentalWorkList create(DentalWork dentalWork) {
        DentalWorkList dentalWorkList = new DentalWorkList();
        dentalWorkList.setUserId(dentalWork.getUserId());
        dentalWorkList.add(dentalWork);
        return dentalWorkList;
    }

    public static DentalWorkList create(UUID userId) {
        DentalWorkList dentalWorkList = new DentalWorkList();
        dentalWorkList.setUserId(userId);
        return dentalWorkList;
    }

    public static DentalWorkList create(List<DentalWork> dentalWorks, UUID userId) {
        DentalWorkList dentalWorkList = new DentalWorkList();
        dentalWorkList.setUserId(userId);
        dentalWorks.forEach(dentalWorkList::add);
        return dentalWorkList;
    }

    public void add(DentalWork dentalWork) {
        if (dentalWorks == null) {
            dentalWorks = new HashMap<>();
        }
        dentalWorks.put(dentalWork.getId(), dentalWork);
    }

    public Optional<DentalWork> findById(long id) {
        if (dentalWorks == null) {
            dentalWorks = new HashMap<>();
            return Optional.empty();
        } else {
            return Optional.ofNullable(dentalWorks.get(id));
        }
    }

    public boolean isContains(DentalWork dentalWork) {
        return dentalWorks.containsKey(dentalWork.getId());
    }

    public void remove(long id) {
        dentalWorks.remove(id);
    }

    public List<DentalWork> toList() {
        return dentalWorks.values().stream().toList();
    }

    public int size() {
        if (dentalWorks == null) {
            dentalWorks = new HashMap<>();
        }
        return dentalWorks.size();
    }
}

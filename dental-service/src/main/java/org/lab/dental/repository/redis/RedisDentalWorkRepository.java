package org.lab.dental.repository.redis;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.lab.dental.repository.DentalWorkCacheRepository;
import org.lab.model.DentalWork;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
public class RedisDentalWorkRepository implements DentalWorkCacheRepository {

    private static final String KEY = "DENTAL_WORKS";

    private final RedisTemplate<String, DentalWorkList> redisTemplate;


    @Autowired
    public RedisDentalWorkRepository(RedisTemplate<String, DentalWorkList> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    @PostConstruct
    public void init() {
        Duration duration = Duration.of(15, ChronoUnit.MINUTES);
        redisTemplate.expire(KEY, duration);
        log.info("Cache duration for {} key is set to {}", KEY, duration);
    }


    @Override
    public void save(List<DentalWork> dentalWorks, UUID userId) {
        log.info("DentalWorkList for user '{}' accepted to cache", userId);
        put(DentalWorkList.create(dentalWorks, userId));
        log.info("DentalWorkList [size={}] for user '{}' is cached", dentalWorks.size(), userId);
    }

    @Override
    public void save(DentalWork dentalWork) {
        log.info("DentalWork (ID={}) for user '{}' accepted to save", dentalWork.getId(), dentalWork.getUserId());
        Optional<DentalWorkList> optionalDentalWorkList = getDentalWorkList(dentalWork.getUserId());
        DentalWorkList dentalWorks;
        if (optionalDentalWorkList.isPresent()) {
            dentalWorks = optionalDentalWorkList.get();
            dentalWorks.add(dentalWork);
        } else {
            dentalWorks = DentalWorkList.create(dentalWork);
        }
        put(dentalWorks);
        log.info("DentalWork (ID={}) for user '{}' is saved", dentalWork.getId(), dentalWork.getUserId());
    }

    @Override
    public List<DentalWork> getAll(UUID userId) {
        Object found = redisTemplate.opsForHash().get(KEY, userId.toString());
        if (found == null) {
            return List.of();
        }
        DentalWorkList dentalWorks = (DentalWorkList) found;
        log.info("Found DentalWorkList [size={}] for user '{}'", dentalWorks.size(), userId);
        return dentalWorks.toList();
    }

    @Override
    public Optional<DentalWork> getByIdAndUserId(long id, UUID userId) {
        Object found = redisTemplate.opsForHash().get(KEY, userId.toString());
        if (found == null) {
            return Optional.empty();
        }
        DentalWorkList dentalWorks = (DentalWorkList) found;
        log.info("Found DentalWorkList [size={}] for user '{}'", dentalWorks.size(), userId);
        return dentalWorks.findById(id);
    }

    @Override
    public void updateIfContains(DentalWork dentalWork) {
        log.info("DentalWork for user '{}' accepted to update if contains", dentalWork.getUserId());
        Optional<DentalWorkList> optionalDentalWorkList = getDentalWorkList(dentalWork.getUserId());
        DentalWorkList dentalWorks;
        if (optionalDentalWorkList.isPresent()) {
            dentalWorks = optionalDentalWorkList.get();
            if (dentalWorks.isContains(dentalWork)) {
                dentalWorks.add(dentalWork);
                put(dentalWorks);
                log.info("DentalWork (ID={}) for user '{}' is updated", dentalWork.getId(), dentalWork.getUserId());
            }
        }
    }

    @Override
    public void delete(long workId, UUID userId) {
        getDentalWorkList(userId).ifPresent(dentalWorkList -> {
            dentalWorkList.remove(workId);
            put(dentalWorkList);
        });
    }


    private void put(DentalWorkList dentalWorks) {
        redisTemplate.opsForHash().put(KEY, dentalWorks.getUserId().toString(), dentalWorks);
    }

    private Optional<DentalWorkList> getDentalWorkList(UUID userId) {
        Object found = redisTemplate.opsForHash().get(KEY, userId.toString());
        if (found == null) {
            return Optional.empty();
        }
        DentalWorkList dentalWorks = (DentalWorkList) found;
        log.info("Found DentalWorkList [size={}] for user '{}'", dentalWorks.size(), userId);
        return Optional.of(dentalWorks);
    }
}

package org.lab.telegram_bot.datasource.redis;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.lab.model.DentalWork;
import org.lab.telegram_bot.datasource.DentalWorkRepository;
import org.lab.telegram_bot.utils.DentalWorkList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
public class RedisDentalWorkRepository implements DentalWorkRepository {

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
    public void save(DentalWorkList dentalWorks) {
        log.info("DentalWorkList for user '{}' accepted to cache", dentalWorks.getUserId());
        redisTemplate.opsForHash().put(KEY, dentalWorks.getUserId().toString(), dentalWorks);
        log.info("DentalWorkList [size={}] for user '{}' is cached", dentalWorks.size(), dentalWorks.getUserId());
    }

    @Override
    public Optional<DentalWorkList> getAll(UUID userId) {
        Object found = redisTemplate.opsForHash().get(KEY, userId.toString());
        if (found == null) {
            return Optional.empty();
        }
        DentalWorkList dentalWorks = (DentalWorkList) found;
        log.info("Found DentalWorkList [size={}] for user '{}'", dentalWorks.size(), userId);
        return Optional.of(dentalWorks);
    }

    @Override
    public void updateIfContains(DentalWork dentalWork) {
        log.info("DentalWork for user '{}' accepted to update if contains", dentalWork.getUserId());
        Optional<DentalWorkList> optionalDentalWorkList = getAll(dentalWork.getUserId());
        DentalWorkList dentalWorks;
        if (optionalDentalWorkList.isPresent()) {
            dentalWorks = optionalDentalWorkList.get();
            if (dentalWorks.isContains(dentalWork)) {
                dentalWorks.add(dentalWork);
                redisTemplate.opsForHash().put(KEY, dentalWorks.getUserId().toString(), dentalWorks);
                log.info("DentalWork with ID={} for user '{}' is updated", dentalWork.getId(), dentalWork.getUserId());
            }
        }
    }

    @Override
    public void delete(long workId, UUID userId) {
        getAll(userId).ifPresent(dentalWorkList -> dentalWorkList.remove(workId));
    }
}

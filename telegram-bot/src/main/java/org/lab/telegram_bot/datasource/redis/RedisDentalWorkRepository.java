package org.lab.telegram_bot.datasource.redis;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.lab.model.DentalWork;
import org.lab.telegram_bot.datasource.DentalWorkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import java.time.Duration;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
    public void save(List<DentalWork> dentalWorks, YearMonth yearMonth, UUID userId) {
        log.info("DentalWorkList by year-month={} for user '{}' accepted to cache", yearMonth, userId);
        put(DentalWorkList.create(dentalWorks, userId), yearMonth);
    }

    @Override
    public void save(DentalWork dentalWork, YearMonth yearMonth) {
        log.info("DentalWork (ID={}) by year-month={} for user '{}' accepted to save", dentalWork.getId(), yearMonth, dentalWork.getUserId());
        Optional<DentalWorkList> optionalDentalWorkList = getDentalWorkList(dentalWork.getUserId(), yearMonth);
        DentalWorkList dentalWorks;
        if (optionalDentalWorkList.isPresent()) {
            dentalWorks = optionalDentalWorkList.get();
            dentalWorks.add(dentalWork);
        } else {
            dentalWorks = DentalWorkList.create(dentalWork);
        }
        put(dentalWorks, yearMonth);
    }

    @Override
    public List<DentalWork> getAll(UUID userId, YearMonth yearMonth) {
        String hashKey = userId.toString() + '_' + yearMonth.toString();
        Object found = redisTemplate.opsForHash().get(KEY, hashKey);
        if (found == null) {
            return List.of();
        }
        DentalWorkList dentalWorks = (DentalWorkList) found;
        log.info("Found DentalWorkList [size={}] by year-month={} for user '{}'", dentalWorks.size(), yearMonth, userId);
        return dentalWorks.toList();
    }

    @Override
    public Optional<DentalWork> getByIdAndUserId(long id, YearMonth yearMonth, UUID userId) {
        String hashKey = userId.toString() + '_' + yearMonth.toString();
        Object found = redisTemplate.opsForHash().get(KEY, hashKey);
        if (found == null) {
            return Optional.empty();
        }
        DentalWorkList dentalWorks = (DentalWorkList) found;
        log.info("Found DentalWorkList [size={}] by year-month={} for user '{}'", dentalWorks.size(), yearMonth, userId);
        return dentalWorks.findById(id);
    }

    @Override
    public void updateIfContains(DentalWork dentalWork, YearMonth yearMonth) {
        log.info("DentalWork by year-month={} for user '{}' accepted to update if contains", yearMonth, dentalWork.getUserId());
        Optional<DentalWorkList> optionalDentalWorkList = getDentalWorkList(dentalWork.getUserId(), yearMonth);
        DentalWorkList dentalWorks;
        if (optionalDentalWorkList.isPresent()) {
            dentalWorks = optionalDentalWorkList.get();
            if (dentalWorks.isContains(dentalWork)) {
                dentalWorks.add(dentalWork);
                put(dentalWorks, yearMonth);
            }
        }
    }

    @Override
    public void delete(long workId, YearMonth yearMonth, UUID userId) {
        getDentalWorkList(userId, yearMonth).ifPresent(dentalWorkList -> {
            dentalWorkList.remove(workId);
            put(dentalWorkList, yearMonth);
        });
    }


    private void put(DentalWorkList dentalWorks, YearMonth yearMonth) {
        String hashKey = dentalWorks.getUserId().toString() + '_' + yearMonth.toString();
        redisTemplate.opsForHash().put(KEY, hashKey, dentalWorks);
        log.info("DentalWorkList [size={}] by year-month={} for user '{}' is cached", dentalWorks.size(), yearMonth, dentalWorks.getUserId());
    }

    private Optional<DentalWorkList> getDentalWorkList(UUID userId, YearMonth yearMonth) {
        String hashKey = userId.toString() + '_' + yearMonth.toString();
        Object found = redisTemplate.opsForHash().get(KEY, hashKey);
        if (found == null) {
            return Optional.empty();
        }
        DentalWorkList dentalWorks = (DentalWorkList) found;
        log.info("Found DentalWorkList [size={}] by year-month={} for user '{}'", dentalWorks.size(), yearMonth, userId);
        return Optional.of(dentalWorks);
    }
}

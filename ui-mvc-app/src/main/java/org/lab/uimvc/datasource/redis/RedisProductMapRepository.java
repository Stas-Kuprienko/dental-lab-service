package org.lab.uimvc.datasource.redis;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.lab.model.ProductMap;
import org.lab.uimvc.datasource.ProductMapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
public class RedisProductMapRepository implements ProductMapRepository {

    private static final String KEY = "PRODUCT_MAP";

    private final RedisTemplate<String, ProductMap> redisTemplate;


    @Autowired
    public RedisProductMapRepository(RedisTemplate<String, ProductMap> productMapRedisTemplate) {
        this.redisTemplate = productMapRedisTemplate;
    }


    @PostConstruct
    public void init() {
        Duration duration = Duration.of(15, ChronoUnit.MINUTES);
        redisTemplate.expire(KEY, duration);
        log.info("Cache duration for {} key is set to {}", KEY, duration);
    }


    @Override
    public void save(ProductMap map) {
        log.info("ProductMap for user '{}' accepted to cache", map.getUserId());
        redisTemplate.opsForHash().put(KEY, map.getUserId().toString(), map);
        log.info("ProductMap for user '{}' is cached", map.getUserId());
    }

    @Override
    public Optional<ProductMap> get(UUID userId) {
        ProductMap map = (ProductMap) redisTemplate.opsForHash().get(KEY, userId.toString());
        log.info("Found ProductMap [{}] for user '{}'", map, userId);
        return Optional.ofNullable(map);
    }
}

package org.lab.telegram_bot.datasource.redis;

import lombok.extern.slf4j.Slf4j;
import org.lab.model.ProductMap;
import org.lab.telegram_bot.datasource.ProductMapRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
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

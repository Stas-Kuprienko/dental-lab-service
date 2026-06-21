package org.lab.dental.repository.redis;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.lab.dental.repository.ProductMapCacheRepository;
import org.lab.model.ProductMap;
import org.lab.model.ProductType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
public class RedisProductMapRepository implements ProductMapCacheRepository {

    private static final String KEY = "PRODUCT_MAP";

    private final RedisTemplate<String, ProductMap> redisTemplate;


    @Autowired
    public RedisProductMapRepository(@Qualifier("productMapRedisTemplate") RedisTemplate<String, ProductMap> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    @PostConstruct
    public void init() {
        Duration duration = Duration.of(15, ChronoUnit.MINUTES);
        redisTemplate.expire(KEY, duration);
        log.info("Cache duration for {} key is set to {}", KEY, duration);
    }


    @Override
    public void save(ProductMap productMap) {
        log.info("ProductMap for user '{}' accepted to cache", productMap.getUserId());
        put(productMap);
        log.info("ProductMap [size={}] for user '{}' is cached", productMap.getEntries().size(), productMap.getUserId());
    }

    @Override
    public Optional<ProductMap> save(ProductType productType, UUID userId) {
        log.info("ProductType ID='{}' for userID='{}' accepted to cache", productType.getId(), userId);
        Optional<ProductMap> optional = getProductMap(userId);
        ProductMap map;
        if (optional.isPresent()) {
            map = optional.get();
            map.getEntries().add(productType);
            put(map);
            log.info("ProductMap [size={}] for user '{}' is cached", map.getEntries().size(), userId);
            return Optional.of(map);
        } else {
            log.info("ProductMap for user '{}' is not found", userId);
            return Optional.empty();
        }
    }

    @Override
    public Optional<ProductMap> get(UUID userId) {
        return getProductMap(userId);
    }

    @Override
    public Optional<ProductMap> updateIfContains(ProductType productType, UUID userId) {
        log.info("ProductType for user '{}' accepted to update if contains", userId);
        Optional<ProductMap> optional = getProductMap(userId);
        if (optional.isPresent()) {
            ProductMap productMap = optional.get();
            productMap.getEntries().add(productType);
            put(productMap);
            log.info("ProductMap for user '{}' is updated, added entry ID='{}'", userId, productType.getId());
            return Optional.of(productMap);
        } else {
            log.info("ProductMap for user '{}' is not found.", userId);
            return Optional.empty();
        }
    }

    @Override
    public void delete(UUID id, UUID userId) {
        getProductMap(userId).ifPresent(productMap -> {
            productMap.setEntries(
                    productMap.getEntries()
                            .stream()
                            .filter(pt -> {
                                boolean toDelete = pt.getId().equals(id);
                                if (toDelete) {
                                    log.info("ProductType ID='{}' is deleted from ProductMap by userID='{}'", id, userId);
                                    return false;
                                } else {
                                    return true;
                                }
                            })
                            .toList());
            put(productMap);
        });
    }


    private void put(ProductMap productMap) {
        redisTemplate.opsForHash().put(KEY, productMap.getUserId().toString(), productMap);
    }

    private Optional<ProductMap> getProductMap(UUID userId) {
        Object found = redisTemplate.opsForHash().get(KEY, userId.toString());
        if (found == null) {
            return Optional.empty();
        }
        ProductMap map = (ProductMap) found;
        log.info("Found ProductMap [size={}] for user '{}'", map.getEntries().size(), userId);
        return Optional.of(map);
    }
}

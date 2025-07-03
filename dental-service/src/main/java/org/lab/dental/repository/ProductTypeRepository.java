package org.lab.dental.repository;

import jakarta.transaction.Transactional;
import org.lab.dental.entity.ProductTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductTypeRepository extends JpaRepository<ProductTypeEntity, UUID> {


    Optional<ProductTypeEntity> findByIdAndUserId(@Param("id") UUID id,
                                                  @Param("userId") UUID userId);

    List<ProductTypeEntity> findAllByUserId(@Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE dental_lab.product_type SET price = :price WHERE id = :id AND user_id = :userId", nativeQuery = true)
    void updatePrice(@Param("id") UUID id,
                     @Param("userId") UUID userId,
                     @Param("price") BigDecimal price);

    @Modifying
    @Transactional
    void deleteByIdAndUserId(@Param("id") UUID id,
                             @Param("userId") UUID userId);
}

package org.lab.dental.repository;

import org.lab.dental.entity.ProductTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductTypeRepository extends JpaRepository<ProductTypeEntity, UUID> {


    Optional<ProductTypeEntity> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    List<ProductTypeEntity> findAllByUserId(@Param("userId") UUID userId);

    void deleteByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);
}

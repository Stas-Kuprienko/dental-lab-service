package org.lab.dental.repository;

import org.lab.dental.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {


    @Query(value = """
                SELECT p.*
                FROM dental_lab.product p
                JOIN dental_lab.dental_work dw ON p.dental_work_id = dw.id
                WHERE dw.user_id = :userId
                  AND dw.complete_at BETWEEN :start AND :end
            """, nativeQuery = true)
    List<ProductEntity> findAllByUserIdAndMonth(@Param("userId") UUID userId,
                                                @Param("start") LocalDate start,
                                                @Param("end") LocalDate end);
}

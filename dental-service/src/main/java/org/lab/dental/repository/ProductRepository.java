package org.lab.dental.repository;

import jakarta.transaction.Transactional;
import org.lab.dental.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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


    @Modifying
    @Transactional
    @Query(value = "UPDATE dental_lab.product SET complete_at = :completeAt WHERE id = :id AND dental_work_id = :workId", nativeQuery = true)
    void updateCompleteAt(@Param("id") UUID id,
                          @Param("workId") Long workId,
                          @Param("completeAt") LocalDate completeAt);


    @Query(value = """
                SELECT
                    EXTRACT(MONTH FROM dw.complete_at) AS month,
                    SUM(p.price * p.quantity) AS profit
                FROM dental_lab.product p
                JOIN dental_lab.dental_work dw ON p.dental_work_id = dw.id
                WHERE dw.user_id = :userId
                  AND dw.status = 'COMPLETED'
                  AND EXTRACT(YEAR FROM dw.complete_at) = :year
                GROUP BY month
                ORDER BY month
            """, nativeQuery = true)
    List<Object[]> countMonthlyProfit(@Param("userId") UUID userId,
                                      @Param("year") int year);
}

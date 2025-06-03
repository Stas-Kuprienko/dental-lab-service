package org.lab.dental.repository;

import jakarta.transaction.Transactional;
import org.lab.dental.entity.DentalWorkEntity;
import org.lab.enums.WorkStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DentalWorkRepository extends JpaRepository<DentalWorkEntity, Long> {

    @NonNull
    @Override
    @Query("""
            SELECT DISTINCT dw
            FROM DentalWorkEntity dw
            LEFT JOIN FETCH dw.products p
            WHERE dw.id = :id
            """)
    Optional<DentalWorkEntity> findById(@NonNull @Param("id") Long id);


    @Query("""
            SELECT DISTINCT dw
            FROM DentalWorkEntity dw
            LEFT JOIN FETCH dw.products p
            WHERE dw.userId = :userId AND
            
            """)
    List<DentalWorkEntity> findAllForMonthByUserId(@Param("userId") UUID userId);


    @Modifying
    @Transactional
    @Query(value = "UPDATE dental_lab.dental_work SET status = :status WHERE id = :id", nativeQuery = true)
    void updateStatus(@Param("id") Long id,
                      @Param("status") WorkStatus status);
}

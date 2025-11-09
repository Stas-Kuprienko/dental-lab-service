package org.lab.dental.repository;

import jakarta.transaction.Transactional;
import org.lab.dental.entity.DentalWorkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DentalWorkRepository extends JpaRepository<DentalWorkEntity, Long> {


    @NonNull
    @Override
    @Query("""
            SELECT dw
            FROM DentalWorkEntity dw
            LEFT JOIN FETCH dw.products p
            WHERE dw.id = :id
            """)
    Optional<DentalWorkEntity> findById(@NonNull @Param("id") Long id);


    @NonNull
    @Query("""
            SELECT dw
            FROM DentalWorkEntity dw
            LEFT JOIN FETCH dw.products p
            WHERE dw.id = :id
            AND dw.userId = :userId
            """)
    Optional<DentalWorkEntity> findByIdAndUserId(@NonNull @Param("id") Long id, @Param("userId") UUID userId);


    @Query("""
            SELECT DISTINCT dw
            FROM DentalWorkEntity dw
            LEFT JOIN FETCH dw.products p
            WHERE dw.userId = :userId AND
            dw.completeAt >= :from
            """)
    List<DentalWorkEntity> findAllFromMonthByUserId(@Param("userId") UUID userId,
                                                    @Param("from") LocalDate from);


    @Query("""
            SELECT DISTINCT dw
            FROM DentalWorkEntity dw
            LEFT JOIN FETCH dw.products p
            WHERE dw.userId = :userId AND
            dw.completeAt >= :from AND
            dw.status <> 'PAID'
            """)
    List<DentalWorkEntity> findAllFromMonthAndNotPaidByUserId(@Param("userId") UUID userId,
                                                              @Param("from") LocalDate from);


    @Query("""
            SELECT DISTINCT dw
            FROM DentalWorkEntity dw
            LEFT JOIN FETCH dw.products p
            WHERE dw.userId = :userId AND
            dw.completeAt BETWEEN :from AND :to
            """)
    List<DentalWorkEntity> findAllForMonthByUserId(@Param("userId") UUID userId,
                                                   @Param("from") LocalDate from,
                                                   @Param("to") LocalDate to);


    @Query("""
            SELECT DISTINCT dw
            FROM DentalWorkEntity dw
            LEFT JOIN FETCH dw.products p
            WHERE dw.clinic LIKE %:clinic%
            AND dw.userId = :userId
            """)
    List<DentalWorkEntity> findByClinicAndUserId(@Param("userId") UUID userId,
                                                 @Param("clinic") String clinic);


    @Query("""
            SELECT DISTINCT dw
            FROM DentalWorkEntity dw
            LEFT JOIN FETCH dw.products p
            WHERE dw.patient LIKE %:patient%
            AND dw.userId = :userId
            """)
    List<DentalWorkEntity> findByPatientAndUserId(@Param("userId") UUID userId,
                                                  @Param("patient") String patient);

    @Query("""
            SELECT DISTINCT dw
            FROM DentalWorkEntity dw
            LEFT JOIN FETCH dw.products p
            WHERE dw.patient LIKE %:patient%
            AND dw.clinic LIKE %:clinic%
            AND dw.userId = :userId
            """)
    List<DentalWorkEntity> findByClinicAndPatientAndUserId(@Param("userId") UUID userId,
                                                           @Param("clinic") String clinic,
                                                           @Param("patient") String patient);


    @Modifying
    @Transactional
    @Query(value = "UPDATE dental_lab.dental_work SET status = :status WHERE id = :id AND user_id = :userId", nativeQuery = true)
    void updateStatus(@Param("id") Long id,
                      @Param("userId") UUID userId,
                      @Param("status") String status);


    @Modifying
    @Transactional
    @Query(value = "UPDATE dental_lab.dental_work SET complete_at = :completeAt WHERE id = :id AND user_id = :userId", nativeQuery = true)
    void updateCompleteAt(@Param("id") Long id,
                          @Param("userId") UUID userId,
                          @Param("completeAt") LocalDate completeAt);


    @Modifying
    @Transactional
    @Query(value = "DELETE FROM dental_lab.dental_work WHERE id = :id AND user_id = :userId", nativeQuery = true)
    void deleteByIdAndUserId(@Param("id") Long id,
                             @Param("userId") UUID userId);
}

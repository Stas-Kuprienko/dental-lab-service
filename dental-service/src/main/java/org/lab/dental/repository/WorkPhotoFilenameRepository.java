package org.lab.dental.repository;

import jakarta.transaction.Transactional;
import org.lab.dental.entity.WorkPhotoFilenameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface WorkPhotoFilenameRepository extends JpaRepository<WorkPhotoFilenameEntity, String> {


    List<WorkPhotoFilenameEntity> findAllByDentalWorkId(@Param("dentalWorkId") long workId);


    @Query("""
            SELECT wpf.filename
             FROM WorkPhotoFilenameEntity wpf
             WHERE wpf.userId = :userId""")
    List<String> findAllFilenamesByUserId(@Param("userId") UUID userId);


    @Modifying
    @Transactional
    @Query(value = "DELETE FROM dental_lab.photo_filename WHERE user_id = :userId", nativeQuery = true)
    int deleteAllByUserId(@Param("userId") UUID userId);
}

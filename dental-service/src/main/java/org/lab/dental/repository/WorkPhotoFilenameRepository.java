package org.lab.dental.repository;

import org.lab.dental.entity.WorkPhotoFilenameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkPhotoFilenameRepository extends JpaRepository<WorkPhotoFilenameEntity, String> {


    List<WorkPhotoFilenameEntity> findAllByDentalWorkId(@Param("dentalWorkId") long workId);
}

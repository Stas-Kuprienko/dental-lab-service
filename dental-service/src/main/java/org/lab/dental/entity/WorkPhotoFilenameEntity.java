package org.lab.dental.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "photo_filename", schema = "dental_lab")
@Builder
@Getter @Setter
@AllArgsConstructor
public class WorkPhotoFilenameEntity {

    @Id
    @Column(name = "filename")
    private String filename;

    @Column(name = "dental_work_id")
    private Long dentalWorkId;



    public WorkPhotoFilenameEntity() {}


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkPhotoFilenameEntity that)) return false;
        return Objects.equals(filename, that.filename) && Objects.equals(dentalWorkId, that.dentalWorkId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filename, dentalWorkId);
    }

    @Override
    public String toString() {
        return "WorkPhotoFilenameEntity{" +
                "filename='" + filename + '\'' +
                ", dentalWorkId=" + dentalWorkId +
                '}';
    }
}

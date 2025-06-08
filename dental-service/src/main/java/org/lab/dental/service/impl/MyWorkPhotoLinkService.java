package org.lab.dental.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.lab.dental.entity.WorkPhotoFilenameEntity;
import org.lab.dental.repository.WorkPhotoFilenameRepository;
import org.lab.dental.service.S3ClientService;
import org.lab.dental.service.WorkPhotoLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Slf4j
@Service
public class MyWorkPhotoLinkService implements WorkPhotoLinkService {

    private final S3ClientService s3ClientService;
    private final WorkPhotoFilenameRepository repository;


    @Autowired
    public MyWorkPhotoLinkService(S3ClientService s3ClientService, WorkPhotoFilenameRepository repository) {
        this.s3ClientService = s3ClientService;
        this.repository = repository;
    }


    @Override
    public String create(MultipartFile file, long workId) {
        log.info("File for DentalWork ID={} received to save: {}", workId, file);
        String filename = s3ClientService.uploadPhoto(file);
        WorkPhotoFilenameEntity entity = new WorkPhotoFilenameEntity(filename, workId);
        entity = repository.save(entity);
        log.info("Entity saved: {}", entity);
        return filename;
    }

    @Override
    public String getById(String filename) {
        String link = s3ClientService.getFileUrl(filename);
        log.info("File '{}' is found", filename);
        return link;
    }

    @Override
    public List<String> getAllByWorkId(long workId) {
        List<WorkPhotoFilenameEntity> entities = repository.findAllByDentalWorkId(workId);
        List<String> links = entities
                .stream()
                .map(e -> s3ClientService.getFileUrl(e.getFilename()))
                        .toList();
        log.info("Found {} file links by parameters: dentalWorkId='{}'", links.size(), workId);
        return links;
    }

    @Override
    public void delete(String filename, long workId) {
        s3ClientService.deletePhoto(filename);
        repository.deleteByFilenameAndDentalWorkId(filename, workId);
        log.info("Photo file '{}' for dentalWorkId='{}' is deleted", filename, workId);
    }
}

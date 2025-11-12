package org.lab.dental.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.lab.dental.entity.WorkPhotoFilenameEntity;
import org.lab.dental.repository.WorkPhotoFilenameRepository;
import org.lab.dental.service.S3ClientService;
import org.lab.dental.service.WorkPhotoFileService;
import org.lab.exception.ApplicationCustomException;
import org.lab.model.WorkPhotoFileData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MyWorkPhotoFileService implements WorkPhotoFileService {

    private static final String FILENAME_PREFIX_TEMPLATE = "dw_%d_";

    private final S3ClientService s3ClientService;
    private final WorkPhotoFilenameRepository repository;


    @Autowired
    public MyWorkPhotoFileService(S3ClientService s3ClientService, WorkPhotoFilenameRepository repository) {
        this.s3ClientService = s3ClientService;
        this.repository = repository;
    }


    @Override
    public String uploadFile(MultipartFile file, long workId) {
        log.info("File for DentalWork ID={} received to save: {}", workId, file);
        String filename = FILENAME_PREFIX_TEMPLATE.formatted(workId) + file.getOriginalFilename();
        s3ClientService.uploadPhoto(file, filename);
        WorkPhotoFilenameEntity entity = new WorkPhotoFilenameEntity(filename, workId);
        entity = repository.save(entity);
        log.info("Entity saved: {}", entity);
        return getLinkByFilename(filename);
    }

    @Override
    public String getLinkByFilename(String filename) {
        String link = s3ClientService.getFileUrl(filename);
        log.info("File '{}' is found", filename);
        return link;
    }

    @Override
    public List<String> getAllLinksByWorkId(long workId) {
        List<WorkPhotoFilenameEntity> entities = repository.findAllByDentalWorkId(workId);
        List<String> links = entities
                .stream()
                .map(e -> s3ClientService.getFileUrl(e.getFilename()))
                .toList();
        log.info("Found {} file links by parameters: dentalWorkId='{}'", links.size(), workId);
        return links;
    }

    @Override
    public List<String> getAllFilenamesByWorkId(long workId) {
        List<WorkPhotoFilenameEntity> entities = repository.findAllByDentalWorkId(workId);
        List<String> links = entities
                .stream()
                .map(WorkPhotoFilenameEntity::getFilename)
                .toList();
        log.info("Found {} filenames by parameters: dentalWorkId='{}'", links.size(), workId);
        return links;
    }

    @Override
    public WorkPhotoFileData downloadFile(String filename) {
        InputStream inputStream = s3ClientService.download(filename);
        try (inputStream) {
            byte[] bytes = inputStream.readAllBytes();
            log.info("File '{}' is found", filename);
            return new WorkPhotoFileData(bytes, filename);
        } catch (IOException e) {
            throw new ApplicationCustomException(e);
        }
    }

    @Override
    public List<WorkPhotoFileData> downloadAllFiles(List<String> filenameList, long workId) {
        List<WorkPhotoFileData> files = new ArrayList<>();
        for (String filename : filenameList) {
            InputStream inputStream = s3ClientService.download(filename);
            try (inputStream) {
                files.add(new WorkPhotoFileData(inputStream.readAllBytes(), filename));
            } catch (IOException e) {
                throw new ApplicationCustomException(e);
            }
        }
        log.info("Found {} files by parameters: dentalWorkId='{}'", files.size(), workId);
        return files;
    }

    @Override
    public void deleteFile(String filename, long workId) {
        s3ClientService.deletePhoto(filename);
        repository.deleteByFilenameAndDentalWorkId(filename, workId);
        log.info("Photo file '{}' for dentalWorkId='{}' is deleted", filename, workId);
    }
}

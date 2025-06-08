package org.lab.dental.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3ClientService {

    String uploadPhoto(MultipartFile file);

    String getFileUrl(String filename);

    void deletePhoto(String filename);
}

package org.lab.dental.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

public interface S3ClientService {

    void uploadPhoto(MultipartFile file, String filename);

    String getFileUrl(String filename);

    InputStream download(String filename);

    void deletePhoto(String filename);
}

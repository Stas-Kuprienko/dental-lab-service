package org.lab.dental.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface WorkPhotoLinkService {

    String create(MultipartFile file, long workId);

    String getById(String filename);

    List<String> getAllByWorkId(long workId);

    void delete(String link, long workId);
}

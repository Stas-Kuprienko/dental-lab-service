package org.lab.dental.service;

import org.lab.model.WorkPhotoFileData;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface WorkPhotoFileService {

    String uploadFile(MultipartFile file, long workId);

    String getLinkByFilename(String filename);

    List<String> getAllLinksByWorkId(long workId);

    List<String> getAllFilenamesByWorkId(long workId);

    WorkPhotoFileData downloadFile(String filename);

    List<WorkPhotoFileData> downloadAllFiles(List<String> filenames, long workId);

    void deleteFile(String link, long workId);
}

package org.lab.dental.service;

import org.lab.dental.entity.WorkPhotoFilenameEntity;
import org.lab.model.WorkPhotoEntry;
import org.lab.model.WorkPhotoFileData;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public interface WorkPhotoFileService {

    WorkPhotoFilenameEntity uploadFile(MultipartFile file, long workId, UUID userId);

    String getLinkByFilename(String filename);

    List<WorkPhotoEntry> getAllLinksByWorkId(long workId);

    List<String> getAllFilenamesByWorkId(long workId);

    WorkPhotoFileData downloadFile(String filename);

    List<WorkPhotoFileData> downloadAllFiles(List<String> filenames, long workId);

    void deleteFile(String filename);

    void deleteAllForUserId(UUID userId);

    void listenFileUploading(Consumer<Long> action);
}

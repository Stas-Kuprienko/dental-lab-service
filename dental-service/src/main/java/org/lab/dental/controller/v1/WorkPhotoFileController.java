package org.lab.dental.controller.v1;

import lombok.extern.slf4j.Slf4j;
import org.lab.dental.service.WorkPhotoFileService;
import org.lab.model.WorkPhotoFileData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/dental_works/{work_id}/photo")
public class WorkPhotoFileController {

    private final WorkPhotoFileService workPhotoFileService;

    @Autowired
    public WorkPhotoFileController(WorkPhotoFileService workPhotoFileService) {
        this.workPhotoFileService = workPhotoFileService;
    }


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> create(@RequestHeader("X-USER-ID") UUID userId,
                                         @PathVariable("work_id") Long workId,
                                         @RequestParam("file") MultipartFile file) {

        log.info("From user '{}' received request to upload file for DentalWork ID={}", userId, workId);
        String filename = workPhotoFileService.uploadFile(file, workId);
        return ResponseEntity.ok(filename);
    }


    @GetMapping("/{filename}")
    public ResponseEntity<String> findById(@RequestHeader("X-USER-ID") UUID userId,
                                           @PathVariable("work_id") Long workId,
                                           @PathVariable("filename") String filename) {

        log.info("From user '{}' received request to get file link '{}' for DentalWork ID={}", userId, filename, workId);
        return ResponseEntity.ok(workPhotoFileService.getLinkByFilename(filename));
    }


    @GetMapping
    public List<String> findAll(@RequestHeader("X-USER-ID") UUID userId,
                                @PathVariable("work_id") Long workId) {

        log.info("From user '{}' received request to all file links for DentalWork ID={}", userId, workId);
        return workPhotoFileService.getAllLinksByWorkId(workId);
    }


    @GetMapping("/download/{filename}")
    public ResponseEntity<WorkPhotoFileData> download(@RequestHeader("X-USER-ID") UUID userId,
                                                      @PathVariable("work_id") Long workId,
                                                      @PathVariable("filename") String filename) {

        log.info("From user '{}' received request to download file '{}' for DentalWork ID={}", userId, filename, workId);
        return ResponseEntity.ok(workPhotoFileService.downloadFile(filename));
    }


    @GetMapping("/download")
    public ResponseEntity<List<WorkPhotoFileData>> downloadAllById(@RequestHeader("X-USER-ID") UUID userId,
                                                                   @PathVariable("work_id") Long workId) {

        log.info("From user '{}' received request to download all files for DentalWork ID={}", userId, workId);
        List<String> filenames = workPhotoFileService.getAllFilenamesByWorkId(workId);
        List<WorkPhotoFileData> files = workPhotoFileService.downloadAllFiles(filenames, workId);
        return ResponseEntity.ok(files);
    }


    @DeleteMapping("/{filename}")
    public ResponseEntity<Void> delete(@RequestHeader("X-USER-ID") UUID userId,
                                       @PathVariable("work_id") Long workId,
                                       @PathVariable("filename") String filename) {

        log.info("From user '{}' received request to delete file '{}' for DentalWork ID={}", userId, filename, workId);
        workPhotoFileService.deleteFile(filename, workId);
        return ResponseEntity.noContent().build();
    }
}

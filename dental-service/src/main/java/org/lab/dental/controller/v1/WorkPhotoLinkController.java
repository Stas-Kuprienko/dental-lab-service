package org.lab.dental.controller.v1;

import lombok.extern.slf4j.Slf4j;
import org.lab.dental.service.WorkPhotoLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/dental_works/{work_id}/photo")
public class WorkPhotoLinkController {

    private final WorkPhotoLinkService workPhotoLinkService;

    @Autowired
    public WorkPhotoLinkController(WorkPhotoLinkService workPhotoLinkService) {
        this.workPhotoLinkService = workPhotoLinkService;
    }


    @PostMapping
    public ResponseEntity<String> create(@RequestHeader("X-USER-ID") UUID userId,
                                         @PathVariable("work_id") Long workId,
                                         @RequestBody MultipartFile file) {

        log.info("From user '{}' received request to upload file '{}' for DentalWork ID={}", userId, file.getOriginalFilename(), workId);
        String filename = workPhotoLinkService.create(file, workId);
        return ResponseEntity.ok(filename);
    }


    @GetMapping("/{filename}")
    public ResponseEntity<String> findById(@RequestHeader("X-USER-ID") UUID userId,
                                           @PathVariable("work_id") Long workId,
                                           @PathVariable("filename") String filename) {

        log.info("From user '{}' received request to get file '{}' for DentalWork ID={}", userId, filename, workId);
        return ResponseEntity.ok(workPhotoLinkService.getById(filename));
    }


    @GetMapping
    public List<String> findAll(@RequestHeader("X-USER-ID") UUID userId,
                                @PathVariable("work_id") Long workId) {

        log.info("From user '{}' received request to all files for DentalWork ID={}", userId, workId);
        return workPhotoLinkService.getAllByWorkId(workId);
    }


    @DeleteMapping("/{filename}")
    public ResponseEntity<Void> delete(@RequestHeader("X-USER-ID") UUID userId,
                                       @PathVariable("work_id") Long workId,
                                       @PathVariable("filename") String filename) {

        log.info("From user '{}' received request to delete file '{}' for DentalWork ID={}", userId, filename, workId);
        workPhotoLinkService.delete(filename, workId);
        return ResponseEntity.noContent().build();
    }
}

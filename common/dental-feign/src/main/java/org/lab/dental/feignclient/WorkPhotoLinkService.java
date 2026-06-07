package org.lab.dental.feignclient;

import org.lab.model.WorkPhotoEntry;
import org.lab.model.WorkPhotoFileData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

@FeignClient(url = "${project.variables.dental-lab-api.url}", path = "/dental_works", name = "work-photo-service")
public interface WorkPhotoLinkService {


    @PostMapping(path = "/{work_id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String create(@PathVariable("work_id") long workId, @RequestPart("file") byte[] file);

    @PostMapping(path = "/{work_id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String create(@PathVariable("work_id") long workId, @RequestPart("file") MultipartFile file);

    @PostMapping(path = "/{work_id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String create(@PathVariable("work_id") long workId,
                  @RequestPart("file") byte[] file,
                  @RequestHeader("X-USER-ID") UUID userId);

    @GetMapping("/{work_id}/photo/{filename}")
    String findById(@PathVariable("work_id") long workId, @PathVariable("filename") String filename);

    @GetMapping("/{work_id}/photo/{filename}")
    String findById(@PathVariable("work_id") long workId,
                    @PathVariable("filename") String filename,
                    @RequestHeader("X-USER-ID") UUID userId);

    @GetMapping("/{work_id}/photo")
    List<WorkPhotoEntry> findAll(@PathVariable("work_id") long workId);

    @GetMapping("/{work_id}/photo")
    List<WorkPhotoEntry> findAll(@PathVariable("work_id") long workId, @RequestHeader("X-USER-ID") UUID userId);

    @GetMapping("/{work_id}/photo/download/{filename}")
    WorkPhotoFileData download(@PathVariable("work_id") long workId, @PathVariable("filename") String filename);

    @GetMapping("/{work_id}/photo/download/{filename}")
    WorkPhotoFileData download(@PathVariable("work_id") long workId,
                               @PathVariable("filename") String filename,
                               @RequestHeader("X-USER-ID") UUID userId);

    @GetMapping("/{work_id}/photo/download")
    List<WorkPhotoFileData> downloadAllById(@PathVariable("work_id") long workId);

    @GetMapping("/{work_id}/photo/download")
    List<WorkPhotoFileData> downloadAllById(@PathVariable("work_id") long workId, @RequestHeader("X-USER-ID") UUID userId);

    @DeleteMapping("/{work_id}/photo/{filename}")
    void delete(@PathVariable("work_id") long workId, @PathVariable("filename") String filename);

    @DeleteMapping("/{work_id}/photo/{filename}")
    void delete(@PathVariable("work_id") long workId,
                @PathVariable("filename") String filename,
                @RequestHeader("X-USER-ID") UUID userId);
}

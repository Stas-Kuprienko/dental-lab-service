package org.lab.dental.feignclient;

import org.lab.model.WorkPhotoEntry;
import org.lab.model.WorkPhotoFileData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@FeignClient(url = "${project.variables.dental-lab-api.url}", path = "/dental_works")
public interface WorkPhotoLinkService {


    @PostMapping("/{work_id}/photo")
    String create(@PathVariable("work_id") long workId, @RequestParam("file") MultipartFile file);

    @GetMapping("/{work_id}/photo/{filename}")
    String findById(@PathVariable("work_id") long workId, @PathVariable("filename") String filename);

    @GetMapping("/{work_id}/photo")
    List<WorkPhotoEntry> findAll(@PathVariable("work_id") long workId);

    @GetMapping("/{work_id}/photo/download/{filename}")
    WorkPhotoFileData download(@PathVariable("work_id") long workId, @PathVariable("filename") String filename);

    @GetMapping("/{work_id}/photo/download")
    List<WorkPhotoFileData> downloadAllById(@PathVariable("work_id") long workId);

    @DeleteMapping("/{work_id}/photo/{filename}")
    void delete(@PathVariable("work_id") long workId, @PathVariable("filename") String filename);
}

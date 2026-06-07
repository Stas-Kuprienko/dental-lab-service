package org.lab.uimvc.controller;

import org.lab.dental.feignclient.DentalWorkService;
import org.lab.dental.feignclient.WorkPhotoLinkService;
import org.lab.model.DentalWork;
import org.lab.model.WorkPhotoEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Controller
@RequestMapping("/main/dental-works/{id}/photo")
public class WorkPhotoController {

    private static final String URL = "/main/dental-works/%d/photo";

    private final DentalWorkService dentalWorkService;
    private final WorkPhotoLinkService workPhotoLinkService;


    @Autowired
    public WorkPhotoController(DentalWorkService dentalWorkService, WorkPhotoLinkService workPhotoLinkService) {
        this.dentalWorkService = dentalWorkService;
        this.workPhotoLinkService = workPhotoLinkService;
    }


    @GetMapping
    public String viewPhotos(@PathVariable("id") Long workId, Model model) {
        DentalWork work = dentalWorkService.findById(workId);
        List<WorkPhotoEntry> photoEntries = workPhotoLinkService.findAll(workId);
        model.addAttribute("patient", work.getPatient());
        model.addAttribute("clinic", work.getClinic());
        model.addAttribute("workId", workId);
        model.addAttribute("photoEntries", photoEntries);
        return "work-photo";
    }

    @PostMapping("/upload")
    public String uploadPhoto(@PathVariable("id") Long workId,
                              @RequestParam("file") MultipartFile file) {
        workPhotoLinkService.create(workId, file);
        return MvcControllerUtil.REDIRECT + URL.formatted(workId);
    }

    @PostMapping("/delete")
    public String deletePhoto(@PathVariable("id") Long workId,
                              @RequestParam("photo-file") String photoFile) {
        workPhotoLinkService.delete(workId, photoFile);
        return MvcControllerUtil.REDIRECT + URL.formatted(workId);
    }
}

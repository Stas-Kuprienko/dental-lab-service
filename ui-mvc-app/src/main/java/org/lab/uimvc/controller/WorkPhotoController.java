package org.lab.uimvc.controller;

import org.dental.restclient.DentalLabRestClient;
import org.dental.restclient.DentalWorkService;
import org.dental.restclient.WorkPhotoLinkService;
import org.lab.model.DentalWork;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/main/dental-works/{id}/photo")
public class WorkPhotoController {

    private static final String URL = "/main/dental-works/%d/photo";

    private final DentalWorkService dentalWorkService;
    private final WorkPhotoLinkService workPhotoLinkService;


    @Autowired
    public WorkPhotoController(DentalLabRestClient dentalLabRestClient) {
        this.dentalWorkService = dentalLabRestClient.DENTAL_WORKS;
        this.workPhotoLinkService = dentalLabRestClient.PHOTO_LINKS;
    }


    @GetMapping
    public String viewPhotos(@PathVariable("id") Long workId, Model model) {
        DentalWork work = dentalWorkService.findById(workId);
        List<String> photoLinks = workPhotoLinkService.findAllById(workId);
        model.addAttribute("patient", work.getPatient());
        model.addAttribute("clinic", work.getClinic());
        model.addAttribute("workId", workId);
        model.addAttribute("photoLinks", photoLinks);
        return "work-photo";
    }

    @PostMapping("/upload")
    public String uploadPhoto(@PathVariable("id") Long workId,
                              @RequestParam("file") MultipartFile file) throws IOException {
        workPhotoLinkService.create(workId, file.getBytes());
        return MvcControllerUtil.REDIRECT + URL.formatted(workId);
    }

    @PostMapping("/delete")
    public String deletePhoto(@PathVariable("id") Long workId,
                              @RequestParam("photoLink") String photoLink) {
        //TODO
        return MvcControllerUtil.REDIRECT + URL.formatted(workId);
    }
}

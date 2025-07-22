package org.lab.uimvc.controller;

import org.dental.restclient.DentalLabRestClient;
import org.dental.restclient.DentalWorkService;
import org.lab.model.DentalWork;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Controller
@RequestMapping("/main/dental-works/{id}/photo")
public class PhotoController {

    private final DentalWorkService dentalWorkService;

    @Autowired
    public PhotoController(DentalLabRestClient dentalLabRestClient) {
        this.dentalWorkService = dentalLabRestClient.DENTAL_WORKS;
    }

    @GetMapping
    public String viewPhotos(@PathVariable("id") Long workId, Model model) {
        DentalWork work = dentalWorkService.findById(workId);
        model.addAttribute("workId", workId);
        model.addAttribute("photoLinks", work.getPhotoLinks());
        return "work-photo";
    }

    @PostMapping("/upload")
    public String uploadPhoto(@PathVariable("id") Long workId,
                              @RequestParam("file") MultipartFile file) throws IOException {


        return "redirect:/main/photo?id=" + workId;
    }

    @PostMapping("/delete")
    public String deletePhoto(@PathVariable("id") Long workId,
                              @RequestParam("photoLink") String photoLink) {
        return "redirect:/main/photo?id=" + workId;
    }
}

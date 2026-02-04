package org.lab.uimvc.controller;

import jakarta.servlet.http.HttpSession;
import org.lab.uimvc.service.OldRecordsTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@Controller
@RequestMapping("/main/admin/old-transfer")
public class OldRecordTransferController extends MvcControllerUtil {

    private final OldRecordsTransferService service;

    @Autowired
    public OldRecordTransferController(OldRecordsTransferService service) {
        this.service = service;
    }


    @GetMapping
    public String getPage() {
        return "old-transfer";
    }

    @PostMapping
    public String handle(HttpSession session,
                         @RequestParam("file") MultipartFile file) {
        UUID userId = getUserId(session);
        service.transfer(file, userId);
        return REDIRECT + "/main/admin/old-transfer";
    }
}

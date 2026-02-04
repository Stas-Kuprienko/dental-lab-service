package org.lab.dental.controller.v1;

import org.lab.dental.service.DentalWorkManager;
import org.lab.model.DentalWork;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/old-transfer")
public class OldRecordTransferController {

    private final DentalWorkManager dentalWorkManager;

    @Autowired
    public OldRecordTransferController(DentalWorkManager dentalWorkManager) {
        this.dentalWorkManager = dentalWorkManager;
    }


    @PostMapping
    public ResponseEntity<Void> transfer(@RequestAttribute("X-USER-ID") UUID userId,
                                         @RequestBody List<DentalWork> dentalWorks) {
        dentalWorkManager.oldRecordTransfer(dentalWorks, userId);
        return ResponseEntity.noContent().build();
    }
}

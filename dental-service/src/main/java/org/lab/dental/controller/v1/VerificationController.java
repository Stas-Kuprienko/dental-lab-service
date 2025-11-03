package org.lab.dental.controller.v1;

import lombok.extern.slf4j.Slf4j;
import org.lab.dental.entity.EmailVerificationTokenEntity;
import org.lab.dental.service.NotificationService;
import org.lab.dental.service.VerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/verification")
public class VerificationController {

    private final VerificationService verificationService;
    private final NotificationService notificationService;

    @Autowired
    public VerificationController(VerificationService verificationService, NotificationService notificationService) {
        this.verificationService = verificationService;
        this.notificationService = notificationService;
    }


    @PostMapping("/email")
    public ResponseEntity<Void> sendVerificationLink(@RequestHeader("X-USER-ID") UUID userId,
                                                     @RequestBody String email,
                                                     @RequestParam(value = "to-change", defaultValue = "false") boolean toChange) {
        EmailVerificationTokenEntity token = verificationService.createForUserId(userId, email);
        if (toChange) {
            notificationService.sendEmailChangeLink(token);
        } else {
            notificationService.sendEmailVerifyLink(token);
        }
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/email/{token}")
    public ResponseEntity<Boolean> verifyUserEmail(@RequestHeader("X-USER-ID") UUID userId,
                                                   @PathVariable("token") String token,
                                                   @RequestParam(value = "to-change", defaultValue = "false") boolean toChange) {
        boolean result;
        if (toChange) {
            result = verificationService.verifyForChangeEmail(userId, token);
        } else {
            result = verificationService.verifyUserEmail(userId, token);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/email/{email}")
    public ResponseEntity<Boolean> isVerified(@RequestHeader("X-USER-ID") UUID userId,
                                              @PathVariable("email") String email) {
        EmailVerificationTokenEntity verificationToken = verificationService.getByUserId(userId);
        if (verificationToken.getEmail().equals(email)) {
            if (verificationToken.isVerified()) {
                verificationService.deleteByUserId(userId);
                return ResponseEntity.ok(true);
            }
        }
        return ResponseEntity.ok(false);
    }
}

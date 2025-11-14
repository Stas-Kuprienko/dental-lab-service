package org.lab.dental.controller.v1;

import lombok.extern.slf4j.Slf4j;
import org.lab.dental.service.CredentialService;
import org.lab.dental.service.VerificationService;
import org.lab.request.ResetPasswordRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/credentials")
public class CredentialController {

    private final CredentialService credentialService;
    private final VerificationService verificationService;

    @Autowired
    public CredentialController(CredentialService credentialService, VerificationService verificationService) {
        this.credentialService = credentialService;
        this.verificationService = verificationService;
    }


    @PostMapping("/reset-password")
    public ResponseEntity<Void> sendResetPasswordLink(@RequestHeader("X-SERVICE-ID") String serviceId,
                                                      @RequestBody String email) {
        log.info("From service '{}' received request to send ResetPasswordLink", serviceId);
        verificationService.createResetPasswordToken(email);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/reset-password/{token}")
    public ResponseEntity<Boolean> verifyResetPasswordToken(@RequestHeader("X-SERVICE-ID") String serviceId,
                                                            @PathVariable("token") String token,
                                                            @RequestBody String email) {
        boolean result = verificationService.verifyResetPasswordToken(email, token);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestHeader("X-SERVICE-ID") String serviceId,
                                           @RequestBody ResetPasswordRequest request) {
        boolean isVerified = verificationService.isVerifiedResetPasswordToken(request.getEmail());
        if (isVerified) {
            credentialService.resetPassword(request.getEmail(), request.getNewPassword());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatusCode.valueOf(403)).build();
        }
    }

    @DeleteMapping("/reset-password")
    public ResponseEntity<Void> deleteResetPasswordToken(@RequestHeader("X-SERVICE-ID") String serviceId,
                                                         @RequestParam("email") String email) {
        verificationService.deleteResetPasswordToken(email);
        return ResponseEntity.noContent().build();
    }
}

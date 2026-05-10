package org.lab.dental.controller.v1;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.lab.dental.service.CredentialService;
import org.lab.dental.service.VerificationService;
import org.lab.request.ResetPasswordRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Credentials")
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
    public ResponseEntity<Void> sendResetPasswordLink(@RequestAttribute("X-SERVICE-ID") String serviceId,
                                                      @RequestBody String email) {
        log.info("From service '{}' received request to send reset password link", serviceId);
        verificationService.createResetPasswordToken(email);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/reset-password/{token}")
    public ResponseEntity<Boolean> verifyResetPasswordToken(@RequestAttribute("X-SERVICE-ID") String serviceId,
                                                            @PathVariable("token") String token,
                                                            @RequestBody String email) {
        log.info("From service '{}' received request to verify reset password link", serviceId);
        boolean result = verificationService.verifyResetPasswordToken(email, token);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestAttribute("X-SERVICE-ID") String serviceId,
                                           @RequestBody ResetPasswordRequest request) {
        log.info("From service '{}' received request to reset password for user '{}'", serviceId, request.getEmail());
        boolean isVerified = verificationService.isVerifiedResetPasswordToken(request.getEmail());
        if (isVerified) {
            credentialService.resetPassword(request.getEmail(), request.getNewPassword());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatusCode.valueOf(403)).build();
        }
    }

    @DeleteMapping("/reset-password")
    public ResponseEntity<Void> deleteResetPasswordToken(@RequestAttribute("X-SERVICE-ID") String serviceId,
                                                         @RequestParam("email") String email) {
        log.info("From service '{}' received request to delete ResetPassword link for user '{}'", serviceId, email);
        verificationService.deleteResetPasswordToken(email);
        return ResponseEntity.noContent().build();
    }
}

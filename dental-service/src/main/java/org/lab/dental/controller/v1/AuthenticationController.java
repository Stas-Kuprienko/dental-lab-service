package org.lab.dental.controller.v1;

import org.lab.dental.service.CredentialService;
import org.lab.dental.service.VerificationService;
import org.lab.dental.util.LoginUtil;
import org.lab.model.AuthToken;
import org.lab.model.LoginCredential;
import org.lab.request.ClientCredentialsRequest;
import org.lab.request.LoginRequest;
import org.lab.request.RefreshTokenRequest;
import org.lab.request.ResetPasswordRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final CredentialService credentialService;
    private final VerificationService verificationService;

    @Autowired
    public AuthenticationController(CredentialService credentialService, VerificationService verificationService) {
        this.credentialService = credentialService;
        this.verificationService = verificationService;
    }


    @PostMapping("/login")
    public ResponseEntity<LoginCredential> login(@RequestBody LoginRequest request) {
        AuthToken authToken = credentialService.userLogin(request.email(), request.password());
        return ResponseEntity.ok(LoginUtil.buildLoginCredential(authToken));
    }

    @PostMapping("/login/client-id")
    public ResponseEntity<AuthToken> login(@RequestBody ClientCredentialsRequest request) {
        AuthToken token = credentialService.clientLogin(request.getClientId(), request.getClientSecret());
        return ResponseEntity.ok(token);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthToken> refresh(@RequestBody RefreshTokenRequest request) {
        AuthToken authToken = credentialService.refresh(request.refreshToken());
        return ResponseEntity.ok(authToken);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> sendResetPasswordLink(@RequestBody String email) {
        verificationService.createResetPasswordToken(email);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/reset-password/{token}")
    public ResponseEntity<Boolean> verifyResetPasswordToken(@PathVariable("token") String token,
                                                            @RequestBody String email) {
        boolean result = verificationService.verifyResetPasswordToken(email, token);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        boolean isVerified = verificationService.isVerifiedResetPasswordToken(request.getEmail());
        if (isVerified) {
            credentialService.resetPassword(request.getEmail(), request.getNewPassword());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatusCode.valueOf(403)).build();
        }
    }

    @DeleteMapping("/reset-password")
    public ResponseEntity<Void> deleteResetPasswordToken(@RequestParam("email") String email) {
        verificationService.deleteResetPasswordToken(email);
        return ResponseEntity.noContent().build();
    }
}

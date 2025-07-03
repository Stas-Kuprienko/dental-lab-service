package org.lab.dental.controller.v1;

import org.lab.dental.service.CredentialService;
import org.lab.dental.util.LoginUtil;
import org.lab.model.AuthToken;
import org.lab.model.LoginCredential;
import org.lab.request.LoginRequest;
import org.lab.request.RefreshTokenRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final CredentialService credentialService;

    @Autowired
    public AuthenticationController(CredentialService credentialService) {
        this.credentialService = credentialService;
    }


    @PostMapping("/login")
    public ResponseEntity<LoginCredential> login(@RequestBody LoginRequest request) {
        AuthToken authToken = credentialService.authenticate(request.email(), request.password());
        return ResponseEntity.ok(LoginUtil.buildLoginCredential(authToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthToken> refresh(@RequestBody RefreshTokenRequest request) {
        AuthToken authToken = credentialService.refresh(request.refreshToken());
        return ResponseEntity.ok(authToken);
    }
}
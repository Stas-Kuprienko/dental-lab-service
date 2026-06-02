package org.lab.dental.controller.v1;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.lab.dental.service.CredentialService;
import org.lab.dental.util.LoginUtil;
import org.lab.model.AuthToken;
import org.lab.model.LoginCredential;
import org.lab.request.ClientCredentialsRequest;
import org.lab.request.LoginRequest;
import org.lab.request.RefreshTokenRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Authentication")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final CredentialService credentialService;

    @Autowired
    public AuthenticationController(CredentialService credentialService) {
        this.credentialService = credentialService;
    }


    @PostMapping("/login")
    public ResponseEntity<LoginCredential> login(@RequestBody @Valid LoginRequest request) {
        log.info("User '{}' request to log in", request.email());
        AuthToken authToken = credentialService.userLogin(request.email(), request.password());
        return ResponseEntity.ok(LoginUtil.buildLoginCredential(authToken));
    }

    @PostMapping("/login/client-id")
    public ResponseEntity<AuthToken> login(@RequestBody ClientCredentialsRequest request) {
        log.info("Client service '{}' request log in", request.getClientId());
        AuthToken token = credentialService.clientLogin(request.getClientId(), request.getClientSecret());
        return ResponseEntity.ok(token);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthToken> refresh(@RequestBody RefreshTokenRequest request) {
        AuthToken authToken = credentialService.refresh(request.refreshToken());
        return ResponseEntity.ok(authToken);
    }
}

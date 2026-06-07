package org.lab.dental.feignclient;

import org.lab.model.AuthToken;
import org.lab.model.LoginCredential;
import org.lab.request.ClientCredentialsRequest;
import org.lab.request.LoginRequest;
import org.lab.request.RefreshTokenRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(url = "${project.variables.dental-lab-api.url}", path = "/auth", name = "authentication-service")
public interface AuthenticationService {


    @PostMapping("/login")
    LoginCredential login(@RequestBody LoginRequest request);

    @PostMapping("/login/client-id")
    AuthToken login(@RequestBody ClientCredentialsRequest request);

    @PostMapping("/refresh")
    AuthToken refresh(@RequestBody RefreshTokenRequest request);
}

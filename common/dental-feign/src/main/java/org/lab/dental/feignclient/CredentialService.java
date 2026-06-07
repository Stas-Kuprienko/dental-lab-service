package org.lab.dental.feignclient;

import org.lab.request.ResetPasswordRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(url = "${project.variables.dental-lab-api.url}", path = "/credentials", name = "credential-service")
public interface CredentialService {


    @PostMapping("/reset-password")
    void sendResetPasswordLink(@RequestBody String email);

    @PatchMapping("/reset-password/{token}")
    boolean verifyResetPasswordToken(@PathVariable("token") String token, @RequestBody String email);

    @PutMapping("/reset-password")
    void resetPassword(@RequestBody ResetPasswordRequest request);

    @DeleteMapping("/reset-password")
    void deleteResetPasswordToken(@RequestParam("email") String email);
}

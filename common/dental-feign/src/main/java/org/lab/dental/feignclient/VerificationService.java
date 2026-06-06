package org.lab.dental.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(url = "${project.variables.dental-lab-api.url}", path = "/verification")
public interface VerificationService {


    @PostMapping("/email")
    void sendVerificationLink(@RequestBody String email, @RequestParam(value = "to-change") boolean toChange);

    @PostMapping("/telegram-otp")
    void sendTelegramOtp(@RequestBody String email);

    @PutMapping("/email/{token}")
    boolean verifyUserEmail(@PathVariable("token") String token, @RequestParam(value = "to-change") boolean toChange);

    @PostMapping("/email/{email}")
    boolean isVerified(@PathVariable("email") String email);
}

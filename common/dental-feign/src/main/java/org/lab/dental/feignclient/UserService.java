package org.lab.dental.feignclient;

import org.lab.enums.MailingType;
import org.lab.model.User;
import org.lab.request.NewUser;
import org.lab.request.UpdatePasswordRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(url = "${project.variables.dental-lab-api.url}", path = "/users", name = "user-service")
public interface UserService {


    @PostMapping
    User signUp(@RequestBody NewUser newUser);

    @PostMapping
    User signUp(@RequestBody NewUser newUser, @RequestHeader("X-USER-ID") UUID userId);

    @GetMapping
    User getById();

    @GetMapping
    User getById(@RequestHeader("X-USER-ID") UUID userId);

    @PutMapping("/name")
    void updateName(@RequestBody String name);

    @PutMapping("/name")
    void updateName(@RequestBody String name, @RequestHeader("X-USER-ID") UUID userId);

    @PutMapping("/email")
    void updateEmail(@RequestBody String email);

    @PutMapping("/email")
    void updateEmail(@RequestBody String email, @RequestHeader("X-USER-ID") UUID userId);

    @PutMapping("/password")
    void updatePassword(@RequestBody UpdatePasswordRequest request);

    @PutMapping("/password")
    void updatePassword(@RequestBody UpdatePasswordRequest request, @RequestHeader("X-USER-ID") UUID userId);

    @PostMapping("/logout")
    void logout();

    @PostMapping("/logout")
    void logout(@RequestHeader("X-USER-ID") UUID userId);

    @DeleteMapping
    void delete();

    @DeleteMapping
    void delete(@RequestHeader("X-USER-ID") UUID userId);

    @PutMapping("/notification/subscribe")
    void subscribeForNotifications(@RequestParam(name = "type") MailingType type);

    @PutMapping("/notification/subscribe")
    void subscribeForNotifications(@RequestParam(name = "type") MailingType type, @RequestHeader("X-USER-ID") UUID userId);

    @PutMapping("/notification/unsubscribe")
    void unsubscribeForNotifications();

    @PutMapping("/notification/unsubscribe")
    void unsubscribeForNotifications(@RequestHeader("X-USER-ID") UUID userId);
}

package org.lab.dental.feignclient;

import org.lab.enums.MailingType;
import org.lab.model.User;
import org.lab.request.NewUser;
import org.lab.request.UpdatePasswordRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(url = "${project.variables.dental-lab-api.url}", path = "/users", name = "user-service")
public interface UserService {


    @PostMapping
    User signUp(@RequestBody NewUser newUser);

    @GetMapping
    User getById();

    @PutMapping("/name")
    void updateName(@RequestBody String name);

    @PutMapping("/email")
    void updateEmail(@RequestBody String email);

    @PutMapping("/password")
    void updatePassword(@RequestBody UpdatePasswordRequest request);

    @PostMapping("/logout")
    void logout();

    @DeleteMapping
    void delete();

    @PutMapping("/notification/subscribe")
    void subscribeForNotifications(@RequestParam(name = "type") MailingType type);

    @PutMapping("/notification/unsubscribe")
    void unsubscribeForNotifications();
}

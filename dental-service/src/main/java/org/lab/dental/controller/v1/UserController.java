package org.lab.dental.controller.v1;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.lab.dental.entity.UserEntity;
import org.lab.dental.mapping.UserConverter;
import org.lab.dental.service.UserService;
import org.lab.dental.util.RequestMappingReader;
import org.lab.model.User;
import org.lab.request.NewUser;
import org.lab.request.UpdatePasswordRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@Slf4j
@Tag(name = "User Data")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final String URL;

    private final UserService userService;
    private final UserConverter userConverter;


    @Autowired
    public UserController(UserService userService, UserConverter userConverter) {
        this.userService = userService;
        this.userConverter = userConverter;
        URL = RequestMappingReader.read(this.getClass());
    }


    @PostMapping
    public ResponseEntity<User> create(@RequestAttribute("X-SERVICE-ID") String serviceId,
                                       @RequestBody @Valid NewUser newUser) {
        log.info("From service '{}' received request to create user with email='{}'", serviceId, newUser.getLogin());
        UserEntity entity = userService.create(newUser.getLogin(), newUser.getName(), newUser.getPassword());
        return ResponseEntity
                .created(URI.create(URL + '/' + entity.getId()))
                .body(userConverter.toDto(entity));
    }

    @GetMapping
    public ResponseEntity<User> getUser(@RequestAttribute("X-USER-ID") UUID userId) {
        log.info("From user '{}' received request to get user data", userId);
        UserEntity entity = userService.getById(userId);
        return ResponseEntity.ok(userConverter.toDto(entity));
    }

    @PutMapping("/name")
    public ResponseEntity<Void> updateName(@RequestAttribute("X-USER-ID") UUID userId,
                                           @RequestBody String name) {
        log.info("From user '{}' received request to update name", userId);
        userService.updateName(userId, name);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/email")
    public ResponseEntity<Void> updateEmail(@RequestAttribute("X-USER-ID") UUID userId,
                                            @RequestBody String email) {
        log.info("From user '{}' received request to update email", userId);
        userService.updateLogin(userId, email);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@RequestAttribute("X-USER-ID") UUID userId,
                                               @RequestBody UpdatePasswordRequest request) {
        log.info("From user '{}' received request to update password", userId);
        userService.updatePassword(
                        userId,
                        request.getEmail(),
                        request.getOldPassword(),
                        request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logoutById(@RequestAttribute("X-USER-ID") UUID userId) {
        log.info("From user '{}' received request to log out", userId);
        userService.logoutById(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestAttribute("X-USER-ID") UUID userId) {
        log.info("Received request to delete user '{}'", userId);
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }
}

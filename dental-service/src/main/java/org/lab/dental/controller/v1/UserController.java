package org.lab.dental.controller.v1;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.lab.dental.entity.UserEntity;
import org.lab.dental.mapping.UserConverter;
import org.lab.dental.service.UserService;
import org.lab.dental.util.RequestMappingReader;
import org.lab.model.User;
import org.lab.request.NewUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.UUID;

@Slf4j
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
    public ResponseEntity<User> create(@RequestBody @Valid NewUser newUser) {
        UserEntity entity = userConverter.fromRequest(newUser);
        entity = userService.create(entity);
        return ResponseEntity.created(URI.create(URL + '/' + entity.getId())).body(userConverter.toDto(entity));
    }


    @GetMapping("/{id}")
    public ResponseEntity<User> findById(@PathVariable("id") UUID id) {
        UserEntity entity = userService.getById(id);
        return ResponseEntity.ok(userConverter.toDto(entity));
    }
}

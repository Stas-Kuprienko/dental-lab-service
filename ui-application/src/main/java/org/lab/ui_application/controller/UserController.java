package org.lab.ui_application.controller;

import org.dental.restclient.DentalLabRestClient;
import org.dental.restclient.UserService;
import org.lab.request.NewUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController extends MvcControllerUtil {

    private final UserService userService;

    @Autowired
    public UserController(DentalLabRestClient dentalLabRestClient) {
        userService = dentalLabRestClient.USERS;
    }


    @GetMapping("/sign-up")
    public String signUpPage() {
        return SIGN_UP_PAGE;
    }

    @PostMapping("/sign-up")
    public String signUpProcess(@ModelAttribute NewUser newUser) {
        userService.signUp(newUser);
        return REDIRECT + MAIN_PATH;
    }
}

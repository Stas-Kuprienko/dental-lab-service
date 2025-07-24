package org.lab.uimvc.controller;

import org.dental.restclient.DentalLabRestClient;
import org.dental.restclient.UserService;
import org.lab.model.User;
import org.lab.request.NewUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthenticationController extends MvcControllerUtil {

    private final UserService userService;

    @Autowired
    public AuthenticationController(DentalLabRestClient dentalLabRestClient) {
        userService = dentalLabRestClient.USERS;
    }


    @GetMapping("/login")
    public String login() {
        return REDIRECT + LOGIN_PATH;
    }

    @GetMapping("/sign-up")
    public String signUpPage() {
        return SIGN_UP_PAGE;
    }

    @PostMapping("/sign-up")
    public String signUpProcess(@ModelAttribute NewUser newUser, Model model) {
        User user = userService.signUp(newUser);
        model.addAttribute("user", user);
        return REDIRECT + USER_PROFILE_PAGE;
    }

    @GetMapping("/main")
    public String mainPage() {
        return MAIN_PAGE;
    }
}

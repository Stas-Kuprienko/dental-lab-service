package org.lab.uimvc.controller;

import org.dental.restclient.AuthenticationService;
import org.dental.restclient.DentalLabRestClient;
import org.dental.restclient.UserService;
import org.lab.model.ErrorResponse;
import org.lab.model.User;
import org.lab.request.NewUser;
import org.lab.request.ResetPasswordRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthenticationController extends MvcControllerUtil {

    private static final String RESET_PASSWORD_LINK_SENT = "RESET_PASSWORD_LINK_SENT";
    private static final String PASSWORDS_NOT_CONFIRMED = "PASSWORDS_NOT_CONFIRMED";
    private static final String WRONG_TOKEN = "WRONG_TOKEN";
    private static final String PASSWORD_IS_UPDATED = "PASSWORD_IS_UPDATED";

    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final MessageSource messageSource;


    @Autowired
    public AuthenticationController(DentalLabRestClient dentalLabRestClient, MessageSource messageSource) {
        this.userService = dentalLabRestClient.USERS;
        this.authenticationService = dentalLabRestClient.AUTHENTICATION;
        this.messageSource = messageSource;
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
        return REDIRECT + LOGIN_PATH + "?login_hint=" + user.getLogin();
    }

    @GetMapping("/log-out")
    public String logout() {
        userService.logout();
        return FORWARD + LOGOUT;
    }

    @GetMapping("/auth/reset-password")
    public String resetPassword() {
        return "reset-password";
    }

    @PostMapping("/auth/reset-password")
    public String resetPassword(@RequestParam("login") String email, RedirectAttributes redirect) {
        authenticationService.sendResetPasswordLink(email);
        String message = messageSource.getMessage(RESET_PASSWORD_LINK_SENT, null, DEFAULT_LOCALE);
        redirect.addFlashAttribute("message", message);
        return REDIRECT + "/auth/reset-password";
    }

    @GetMapping("/auth/reset-password-verify")
    public String verifyToken(@RequestParam("email") String email, @RequestParam("token") String token, Model model) {
        boolean result = authenticationService.verifyResetPasswordToken(token, email);
        if (result) {
            model.addAttribute("email", email);
            model.addAttribute("token", token);
            return "change-password";
        } else {
            String message = messageSource.getMessage(WRONG_TOKEN, null, DEFAULT_LOCALE);
            ErrorResponse error = ErrorResponse.builder()
                    .status(HttpStatus.BAD_REQUEST.name())
                    .message(message)
                    .build();
            model.addAttribute("error", error);
            return "error";
        }
    }

    @PostMapping("/auth/change-password")
    public String changePassword(@RequestParam("token") String token,
                                 @RequestParam("email") String email,
                                 @RequestParam("new-password") String newPassword,
                                 @RequestParam("confirm-password") String confirmPassword,
                                 RedirectAttributes redirect) {
        if (!newPassword.equals(confirmPassword)) {
            String message = messageSource.getMessage(PASSWORDS_NOT_CONFIRMED, null, DEFAULT_LOCALE);
            redirect.addFlashAttribute("error", message);
            return REDIRECT + "change-password";
        } else {
            ResetPasswordRequest request = ResetPasswordRequest.builder()
                    .email(email)
                    .newPassword(newPassword)
                    .build();
            authenticationService.resetPassword(request);
            String message = messageSource.getMessage(PASSWORD_IS_UPDATED, null, DEFAULT_LOCALE);
            redirect.addFlashAttribute("infoMessage", message);
            return REDIRECT + LOGIN_PATH;
        }
    }

    @GetMapping("/main")
    public String mainPage() {
        return MAIN_PAGE;
    }
}

package org.lab.uimvc.controller;

import jakarta.servlet.http.HttpSession;
import org.dental.restclient.DentalLabRestClient;
import org.dental.restclient.UserService;
import org.dental.restclient.VerificationService;
import org.lab.model.ErrorResponse;
import org.lab.model.User;
import org.lab.request.UpdatePasswordRequest;
import org.lab.uimvc.configuration.auth.CredentialsUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/main/user")
public class UserProfileController extends MvcControllerUtil {

    private static final String CHANGE_EMAIL_LINK_SENT = "CHANGE_EMAIL_LINK_SENT";
    private static final String WRONG_TOKEN = "WRONG_TOKEN";
    private static final String EMAIL_IS_UPDATED = "EMAIL_IS_UPDATED";
    private static final String TOKEN_FOR_CHANGE_EMAIL_NOT_VERIFIED = "TOKEN_FOR_CHANGE_EMAIL_NOT_VERIFIED";
    private static final String PASSWORDS_NOT_CONFIRMED = "PASSWORDS_NOT_CONFIRMED";
    private static final String NEW_AND_OLD_PASSWORDS_EQUALS = "NEW_AND_OLD_PASSWORDS_EQUALS";
    private static final String WRONG_PASSWORD = "WRONG_PASSWORD";
    private static final String PASSWORD_IS_UPDATED = "PASSWORD_IS_UPDATED";

    private final UserService userService;
    private final VerificationService verificationService;
    private final CredentialsUtility credentialsUtility;
    private final MessageSource messageSource;


    @Autowired
    public UserProfileController(DentalLabRestClient dentalLabRestClient,
                                 CredentialsUtility credentialsUtility,
                                 MessageSource messageSource) {
        userService = dentalLabRestClient.USERS;
        verificationService = dentalLabRestClient.VERIFICATION;
        this.credentialsUtility = credentialsUtility;
        this.messageSource = messageSource;
    }


    @GetMapping
    public String getProfile(Model model) {
        User user = userService.get();
        model.addAttribute("user", user);
        return USER_PROFILE_PAGE;
    }

    @PostMapping("/update-name")
    public String updateName(@RequestParam("name") String name, Model model) {
        User user = userService.updateName(name);
        //TODO !!!!!!
        model.addAttribute("user", user);
        return REDIRECT + USER_PROFILE_PAGE;
    }

    @PostMapping("/delete")
    public String deleteProfile() {
        userService.delete();
        return REDIRECT + LOGOUT;
    }

    @PostMapping("/change-email")
    public String sendChangeEmailLink(HttpSession session, RedirectAttributes redirect, Model model) {
        String email = getEmail(session);
        verificationService.sendVerificationLink(email, true);
        String message = messageSource.getMessage(CHANGE_EMAIL_LINK_SENT, null, DEFAULT_LOCALE);
        redirect.addFlashAttribute("message", message);
        User user = userService.get();
        //TODO !!!!!!!!
        model.addAttribute("user", user);
        return REDIRECT + USER_PROFILE_PAGE;
    }

    @GetMapping("/verify")
    public String changeEmailPage(@RequestParam("token") String token, Model model) {
        boolean result = verificationService.verifyUserEmail(token, true);
        if (result) {
            return "change-email";
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

    @PostMapping("/update-email")
    public String updateEmail(@RequestParam("new-email") String newEmail,
                              HttpSession session,
                              Authentication authentication,
                              RedirectAttributes redirect) {
        boolean result = verificationService.isVerified(getEmail(session));
        String messageKey;
        if (result) {
            User user = userService.updateEmail(newEmail);
            //TODO !!!!!!!
            credentialsUtility.updateUserEmail(authentication, newEmail);
            session.setAttribute(ATTRIBUTE_KEY_USER_EMAIL, user.getLogin());
            messageKey = EMAIL_IS_UPDATED;
        } else {
            messageKey = TOKEN_FOR_CHANGE_EMAIL_NOT_VERIFIED;
        }
        String message = messageSource.getMessage(messageKey, null, DEFAULT_LOCALE);
        redirect.addFlashAttribute("message", message);
        return REDIRECT + USER_PROFILE_PAGE;
    }

    @PostMapping("/change-email-otp")
    public String sendEmailOtp(RedirectAttributes redirect) {
        redirect.addFlashAttribute("message", "Иди ты нахуй");
        return REDIRECT + USER_PROFILE_PATH;
    }

    @PostMapping("/change-password")
    public String changePassword(HttpSession session,
                                 @RequestParam("old-password") String oldPassword,
                                 @RequestParam("new-password") String newPassword,
                                 @RequestParam("confirm-password") String confirmPassword,
                                 RedirectAttributes redirect) {
        String messageKey;
        if (!newPassword.equals(confirmPassword)) {
            messageKey = PASSWORDS_NOT_CONFIRMED;
        } else if (oldPassword.equals(newPassword)) {
            messageKey = NEW_AND_OLD_PASSWORDS_EQUALS;
        } else {
            UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                    .email(getEmail(session))
                    .oldPassword(oldPassword)
                    .newPassword(newPassword)
                    .build();
            boolean result = userService.updatePassword(request);
            if (result) {
                messageKey = PASSWORD_IS_UPDATED;
            } else {
                messageKey = WRONG_PASSWORD;
            }
        }
        String message = messageSource.getMessage(messageKey, null, DEFAULT_LOCALE);
        redirect.addFlashAttribute("message", message);
        return REDIRECT + USER_PROFILE_PATH;
    }

    @PostMapping("/reset-password")
    public String requestPasswordReset(@AuthenticationPrincipal UserDetails user,
                                       RedirectAttributes redirect) {
//        String token = passwordService.generateResetToken(user.getUsername());
//        mailService.sendPasswordResetLink(user.getUsername(), token);

        redirect.addFlashAttribute("message", "Ссылка для смены пароля отправлена на почту");
        return "redirect:/main/user";
    }
}

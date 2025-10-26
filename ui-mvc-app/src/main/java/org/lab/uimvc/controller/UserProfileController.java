package org.lab.uimvc.controller;

import jakarta.servlet.http.HttpSession;
import org.dental.restclient.DentalLabRestClient;
import org.dental.restclient.UserService;
import org.lab.model.User;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final UserService userService;

    @Autowired
    public UserProfileController(DentalLabRestClient dentalLabRestClient) {
        userService = dentalLabRestClient.USERS;
    }


    @GetMapping
    public String getProfile(Model model) {
        User user = userService.get();
        model.addAttribute("user", user);
        return USER_PROFILE_PAGE;
    }

    @PostMapping("/update-name")
    public String updateName(@RequestParam String name, Model model) {
        User user = userService.updateName(name);
        model.addAttribute("user", user);
        return REDIRECT + USER_PROFILE_PAGE;
    }

    @PostMapping("/delete")
    public String deleteProfile() {
        userService.delete();
        return "redirect:/logout";
    }

    @GetMapping("/change-email")
    public String changeEmailPage(Model model) {
        model.addAttribute("step", 1);
        return "change-email";
    }

    @PostMapping("/change-email")
    public String requestEmailChange(RedirectAttributes redirect) {
        userService.changeEmail();
        redirect.addFlashAttribute("message", "Код отправлен в Telegram");
        return "redirect:/main/user/change-email";
    }

    @PostMapping("/main/user/verify-email-code")
    public String verifyEmailCode(@RequestParam String code,
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes redirect) {
        if (userService.verifyEmailChangeCode(code)) {
            session.setAttribute("emailVerified", true);
            model.addAttribute("step", 2);
            return "change-email";
        } else {
            redirect.addFlashAttribute("error", "Неверный код");
            return "redirect:/main/user/change-email";
        }
    }

    @PostMapping("/main/user/save-email")
    public String saveNewEmail(@RequestParam String newEmail,
                               HttpSession session,
                               @AuthenticationPrincipal UserDetails user,
                               RedirectAttributes redirect) {
        if (!Boolean.TRUE.equals(session.getAttribute("emailVerified"))) {
            redirect.addFlashAttribute("error", "Сначала введите код из Telegram");
            return "redirect:/main/user/change-email";
        }

        userService.changeEmail();
        session.removeAttribute("emailVerified");

        redirect.addFlashAttribute("message", "Email успешно обновлён");
        return "redirect:/main/user";
    }

    @PostMapping("/main/user/request-password-reset")
    public String requestPasswordReset(@AuthenticationPrincipal UserDetails user,
                                       RedirectAttributes redirect) {
//        String token = passwordService.generateResetToken(user.getUsername());
//        mailService.sendPasswordResetLink(user.getUsername(), token);

        redirect.addFlashAttribute("message", "Ссылка для смены пароля отправлена на почту");
        return "redirect:/main/user";
    }

    @GetMapping("/change-password")
    public String changePasswordPage(@RequestParam String token, Model model) {
//        if (!passwordService.isTokenValid(token)) {
//            model.addAttribute("error", "Неверный или устаревший токен");
//            return "reset-password";
//        }
//        model.addAttribute("token", token);
        return "change-password";
    }

    @PostMapping("/main/user/save-new-password")
    public String saveNewPassword(@RequestParam String token,
                                  @RequestParam String newPassword,
                                  RedirectAttributes redirect) {
//        if (!passwordService.isTokenValid(token)) {
//            redirect.addFlashAttribute("error", "Неверный токен");
//            return "redirect:/main/user/reset-password?token=" + token;
//        }

//        passwordService.resetPassword(token, newPassword);
//        redirect.addFlashAttribute("message", "Пароль успешно обновлён");
        return "redirect:/main/user";
    }
}

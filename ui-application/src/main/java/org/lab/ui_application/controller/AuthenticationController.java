package org.lab.ui_application.controller;

import jakarta.servlet.http.HttpSession;
import org.dental.restclient.AuthenticationService;
import org.dental.restclient.DentalLabRestClient;
import org.lab.model.LoginCredential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class AuthenticationController extends MvcControllerUtil {

    private final AuthenticationService authenticationService;

    @Autowired
    public AuthenticationController(DentalLabRestClient dentalLabRestClient) {
        authenticationService = dentalLabRestClient.AUTHENTICATION;
    }


    @GetMapping("/login")
    public String loginPage() {
        return LOGIN_PAGE;
    }

    @PostMapping("/login")
    public String loginProcess(@RequestParam("email") String email,
                               @RequestParam("password") String password,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        try {
            LoginCredential loginCredential = authenticationService.login(email, password);
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    email, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
            session.setAttribute("ACCESS_TOKEN", loginCredential.getToken().getAccessToken());
            session.setAttribute("REFRESH_TOKEN", loginCredential.getToken().getRefreshToken());
            //TODO
            session.setAttribute("USER_ID", loginCredential.getUserId());
            return REDIRECT + MAIN_PATH;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Неверный логин или пароль");
            return REDIRECT + "/login?error";
        }
    }

    @GetMapping("/main")
    public String mainPage() {
        return MAIN_PAGE;
    }
}

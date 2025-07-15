package org.lab.ui_application.configuration.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.lab.ui_application.controller.MvcControllerUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        if (authentication instanceof OAuth2AuthenticationToken token) {
            OAuth2User user = token.getPrincipal();
            String userId = user.getAttribute("sub");
            String email = user.getAttribute("email");
            String name = user.getAttribute("name");
            HttpSession session = request.getSession();
            session.setAttribute("USER_ID", userId);
            session.setAttribute("USER_EMAIL", email);
            session.setAttribute("USER_NAME", name);
        }
        response.sendRedirect(MvcControllerUtil.MAIN_PATH);
    }
}
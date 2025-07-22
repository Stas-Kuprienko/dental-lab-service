package org.lab.uimvc.configuration.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.lab.uimvc.controller.MvcControllerUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.UUID;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OidcUser user = (OidcUser) authentication.getPrincipal();
        HttpSession session = request.getSession();
        session.setAttribute(MvcControllerUtil.ATTRIBUTE_KEY_USER_ID, UUID.fromString(user.getSubject()));
        session.setAttribute(MvcControllerUtil.ATTRIBUTE_KEY_USER_EMAIL, user.getEmail());
        session.setAttribute(MvcControllerUtil.ATTRIBUTE_KEY_USER_NAME, user.getFullName());
        response.sendRedirect(MvcControllerUtil.MAIN_FULL_PATH);
    }
}

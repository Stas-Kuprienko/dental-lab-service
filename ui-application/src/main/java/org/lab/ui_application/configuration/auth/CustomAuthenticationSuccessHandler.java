package org.lab.ui_application.configuration.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.dental.restclient.DentalLabRestClient;
import org.dental.restclient.ProductMapService;
import org.lab.model.ProductMap;
import org.lab.ui_application.controller.MvcControllerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final ProductMapService productMapService;

    @Autowired
    public CustomAuthenticationSuccessHandler(DentalLabRestClient dentalLabRestClient) {
        this.productMapService = dentalLabRestClient.PRODUCT_MAP;
    }


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OidcUser user = (OidcUser) authentication.getPrincipal();
        HttpSession session = request.getSession();
        session.setAttribute(MvcControllerUtil.ATTRIBUTE_KEY_USER_ID, user.getSubject());
        session.setAttribute(MvcControllerUtil.ATTRIBUTE_KEY_USER_EMAIL, user.getEmail());
        session.setAttribute(MvcControllerUtil.ATTRIBUTE_KEY_USER_NAME, user.getFullName());
        ProductMap map = productMapService.findAll();
        session.setAttribute(MvcControllerUtil.ATTRIBUTE_KEY_MAP, map.getEntries());
        response.sendRedirect(MvcControllerUtil.MAIN_PATH);
    }
}

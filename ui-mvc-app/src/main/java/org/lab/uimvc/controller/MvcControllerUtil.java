package org.lab.uimvc.controller;

import jakarta.servlet.http.HttpSession;
import org.lab.model.ProductMap;
import org.lab.uimvc.service.ProductMapMvcService;
import org.springframework.ui.Model;
import java.util.UUID;

public abstract class MvcControllerUtil {

    public static final String REDIRECT = "redirect:";
    public static final String FORWARD = "forward:";
    public static final String LOGIN_PATH = "/oauth2/authorization/keycloak";
    public static final String SIGN_UP_PAGE = "sign-up";
    public static final String SIGN_UP_PATH = "/sign-up";
    public static final String MAIN_PAGE = "main";
    public static final String MAIN_PATH = "/main";
    public static final String MAIN_FULL_PATH = "/dental-lab/main";
    public static final String ATTRIBUTE_KEY_USER_ID = "USER_ID";
    public static final String ATTRIBUTE_KEY_USER_EMAIL = "USER_EMAIL";
    public static final String ATTRIBUTE_KEY_USER_NAME = "USER_NAME";
    public static final String ATTRIBUTE_KEY_MAP = "map";
    public static final String USER_PROFILE_PAGE = "user-profile";
    public static final String USER_PROFILE_PATH = "/main/profile";



    public static void addProductMapToModel(ProductMapMvcService productMapService, HttpSession session, Model model) {
        UUID userId = (UUID) session.getAttribute(MvcControllerUtil.ATTRIBUTE_KEY_USER_ID);
        ProductMap map = productMapService.get(userId);
        model.addAttribute(ATTRIBUTE_KEY_MAP, map.getEntries());
    }
}

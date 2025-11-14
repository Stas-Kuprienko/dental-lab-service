package org.lab.uimvc.configuration.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.lab.uimvc.controller.MvcControllerUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Slf4j
@Component
public class MyAccessDeniedHandler implements AccessDeniedHandler {


    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception) throws IOException {
        log.info(exception.getMessage() + '\n' + request.getRequestURI());
        String redirect = MvcControllerUtil.REDIRECT + MvcControllerUtil.MAIN_PATH;
        response.sendRedirect(redirect);
    }
}

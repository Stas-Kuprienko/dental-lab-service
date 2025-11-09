package org.lab.uimvc.configuration.auth;

import lombok.extern.slf4j.Slf4j;
import org.lab.exception.InternalCustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class CredentialsUtility {

    private static final String PREVIOUS_EMAIL = "PREVIOUS_EMAIL_FOR:";
    private static final String PREVIOUS_NAME = "PREVIOUS_NAME_FOR:";

    private final Map<String, Object> attributes;


    @Autowired
    public CredentialsUtility() {
        attributes = new ConcurrentHashMap<>();
    }


    public void updateUserEmail(Authentication authentication, String newEmail) {
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        String key = PREVIOUS_EMAIL + oidcUser.getSubject();
        attributes.put(key, oidcUser.getEmail());
        refreshOidcUserEmail(oidcUser, newEmail);
        refreshSecurityContext((OAuth2AuthenticationToken) authentication, oidcUser);
    }

    public void updateUserName(Authentication authentication, String newName) {
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        String key = PREVIOUS_NAME + oidcUser.getSubject();
        attributes.put(key, oidcUser.getName());
        refreshOidcUserName(oidcUser, newName);
        refreshSecurityContext((OAuth2AuthenticationToken) authentication, oidcUser);
    }

    public void rollbackUserEmail(Authentication authentication) {
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        String key = PREVIOUS_EMAIL + oidcUser.getSubject();
        String oldEmail = (String) attributes.get(key);
        if (oldEmail == null) {
            throw new InternalCustomException("Rollback failure! The previous email value was not found for the user: " + oidcUser.getSubject());
        }
        refreshOidcUserEmail(oidcUser, oldEmail);
        refreshSecurityContext((OAuth2AuthenticationToken) authentication, oidcUser);
    }

    public void rollbackUserName(Authentication authentication) {
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        String key = PREVIOUS_NAME + oidcUser.getSubject();
        String oldName = (String) attributes.get(key);
        if (oldName == null) {
            throw new InternalCustomException("Rollback failure! The previous name value was not found for the user: " + oidcUser.getSubject());
        }
        refreshOidcUserName(oidcUser, oldName);
        refreshSecurityContext((OAuth2AuthenticationToken) authentication, oidcUser);
    }


    private void refreshOidcUserEmail(OidcUser oidcUser, Object newValue) {
        String attributesField = "attributes";
        try {
            DefaultOidcUser oAuth2User = (DefaultOidcUser) oidcUser;
            var field = oAuth2User.getClass().getSuperclass().getDeclaredField(attributesField);
            field.setAccessible(true);
            Map<String, Object> map = new HashMap<>(oidcUser.getClaims());
            map.put("email", newValue);
            map.put("preferred_username", newValue);
            field.set(oAuth2User, Collections.unmodifiableMap(new LinkedHashMap<>(map)));
            field.setAccessible(false);
        } catch (ReflectiveOperationException e) {
            throw new InternalCustomException(e);
        }
    }

    private void refreshOidcUserName(OidcUser oidcUser, Object newValue) {
        String attributesField = "attributes";
        try {
            DefaultOidcUser oAuth2User = (DefaultOidcUser) oidcUser;
            var field = oAuth2User.getClass().getSuperclass().getDeclaredField(attributesField);
            field.setAccessible(true);
            Map<String, Object> map = new HashMap<>(oidcUser.getClaims());
            map.put("name", newValue);
            map.put("given_name", newValue);
            field.set(oAuth2User, Collections.unmodifiableMap(new LinkedHashMap<>(map)));
            field.setAccessible(false);
        } catch (ReflectiveOperationException e) {
            throw new InternalCustomException(e);
        }
    }

    private static void refreshSecurityContext(OAuth2AuthenticationToken authentication, OidcUser oidcUser) {
        Authentication newAuth = new OAuth2AuthenticationToken(
                oidcUser,
                oidcUser.getAuthorities(),
                authentication.getAuthorizedClientRegistrationId()
        );
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }
}

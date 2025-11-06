package org.lab.uimvc.configuration.auth;

import org.lab.exception.InternalCustomException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CredentialsUtility {

    private static final String PREVIOUS_EMAIL = "PREVIOUS_EMAIL_FOR:";

    private final Map<String, Object> attributes;


    public CredentialsUtility() {
        attributes = new ConcurrentHashMap<>();
    }


    public void updateUserEmail(Authentication authentication, String newEmail) {
        //TODO !!!!!!!!!!!!!!!
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        String key = PREVIOUS_EMAIL + oidcUser.getSubject();
        attributes.put(key, oidcUser.getEmail());
        oidcUser.getClaims().put("email", newEmail);
    }

    public void rollbackUserEmail(Authentication authentication) {
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        String key = PREVIOUS_EMAIL + oidcUser.getSubject();
        String oldEmail = (String) attributes.get(key);
        if (oldEmail == null) {
            throw new InternalCustomException("Rollback failure! The previous email value was not found for the user: " + oidcUser.getSubject());
        }
        oidcUser.getClaims().put("email", oldEmail);
    }
}

package org.lab.dental.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.lab.model.AuthToken;
import org.lab.model.LoginCredential;
import java.util.UUID;

public final class LoginUtil {

    private LoginUtil() {}


    public static LoginCredential buildLoginCredential(AuthToken token) {
        DecodedJWT jwt = JWT.decode(token.getAccessToken());
        String userId = jwt.getClaim("sub").asString();
        String email = jwt.getClaim("email").asString();
        return LoginCredential.builder()
                .token(token)
                .userId(UUID.fromString(userId))
                .email(email)
                .build();
    }
}

package org.lab.dental.service;

import org.lab.model.AuthToken;
import java.util.UUID;

public interface CredentialService {

    UUID signUp(String login, String password, String name);

    AuthToken authenticate(String email, String password);

    AuthToken refresh(String refreshToken);

    void setPassword(String login, String password);

    void deleteUser(String login);
}

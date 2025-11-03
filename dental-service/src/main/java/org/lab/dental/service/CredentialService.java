package org.lab.dental.service;

import org.lab.model.AuthToken;
import java.util.UUID;

public interface CredentialService {

    UUID signUp(String email, String password, String name);

    AuthToken clientLogin(String clientId, String clientSecret);

    AuthToken userLogin(String email, String password);

    AuthToken refresh(String refreshToken);

    void verifyEmail(UUID userId, String email);

    void updateEmail(UUID userId, String newEmail);

    void setPassword(UUID userId, String email, String oldPassword, String newPassword);

    void deleteUser(UUID userId);
}

package org.lab.dental.service.impl;

import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.lab.dental.service.CredentialService;
import org.lab.exception.BadRequestCustomException;
import org.lab.exception.ForbiddenCustomException;
import org.lab.exception.ApplicationCustomException;
import org.lab.exception.KeycloakEmailDuplicationException;
import org.lab.model.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class KeycloakCredentialService implements CredentialService {

    private static final String TOKEN_URI = "/protocol/openid-connect/token";

    private final RealmResource realmResource;
    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;


    @Autowired
    public KeycloakCredentialService(RealmResource realmResource,
                                     RestClient.Builder restClientBuilder,
                                     @Value("${project.variables.keycloak.url}") String keycloakUrl,
                                     @Value("${project.variables.keycloak.realm}") String keycloakRealm,
                                     @Value("${project.variables.keycloak.client-id}") String clientId,
                                     @Value("${project.variables.keycloak.client-secret}") String clientSecret) {
        this.realmResource = realmResource;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        restClient = restClientBuilder.baseUrl(keycloakUrl + "/realms/" + keycloakRealm).build();
        clientLogin(clientId, clientSecret);
    }


    @Override
    public UUID signUp(String email, String password, String name) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setValue(password);
        credential.setType(CredentialRepresentation.PASSWORD);

        UserRepresentation representation = new UserRepresentation();
        representation.setEmail(email);
        representation.setUsername(email);
        representation.setFirstName(name);
        representation.setCredentials(Collections.singletonList(credential));
        representation.setEnabled(true);
        representation.setClientRoles(Map.of(clientId, List.of("ROLE_USER")));
        representation.setEmailVerified(false);
        representation.setRequiredActions(Collections.emptyList());

        Response response = realmResource.users().create(representation);
        try (response) {
            if (response.getStatus() == 201) {
                log.info("User '{}' is signed up", email);
                return extractId(response);
            } else if (response.getStatus() == 409) {
                throw new KeycloakEmailDuplicationException(email);
            } else {
                String statusInfo = response.getStatusInfo().toString();
                throw ApplicationCustomException.keycloakAuthFail(email, statusInfo);
            }
        }
    }

    @Override
    public AuthToken clientLogin(String clientId, String clientSecret) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "client_credentials");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        AuthToken token = requestToken(params);
        log.info("Keycloak client '{}' is logged in.", clientId);
        return token;
    }

    @Override
    public AuthToken userLogin(String email, String password) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "password");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("username", email);
        params.add("password", password);
        AuthToken token = requestToken(params);
        log.info("Keycloak user '{}' is logged in.", email);
        return token;
    }

    @Override
    public AuthToken refresh(String refreshToken) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "refresh_token");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("refresh_token", refreshToken);
        AuthToken token = requestToken(params);
        log.info("Keycloak client '{}' is refreshed token.", clientId);
        return token;
    }

    @Override
    public void verifyEmail(UUID userId, String email) {
        UserRepresentation representation = realmResource.users().get(userId.toString()).toRepresentation();
        if (representation.getEmail().equals(email)) {
            representation.setEmailVerified(true);
            realmResource
                    .users()
                    .get(userId.toString())
                    .update(representation);
            log.info("Email is verified for user '{}'", userId);
        } else {
            throw new BadRequestCustomException("Passed email not equals to user email");
        }
    }

    @Override
    public void updateEmail(UUID userId, String newEmail) {
        UserRepresentation representation = realmResource.users().get(userId.toString()).toRepresentation();
        representation.setEmail(newEmail);
        representation.setEmailVerified(true);
        realmResource
                .users()
                .get(userId.toString())
                .update(representation);
        log.info("Email is updated for user '{}'", userId);
    }

    @Override
    public void updateName(UUID userId, String newName) {
        UserRepresentation representation = realmResource.users().get(userId.toString()).toRepresentation();
        representation.setFirstName(newName);
        realmResource
                .users()
                .get(userId.toString())
                .update(representation);
        log.info("Name is updated for user '{}'", userId);
    }

    @Override
    public void updatePassword(UUID userId, String email, String oldPassword, String newPassword) {
        try {
            userLogin(email, oldPassword);
            CredentialRepresentation representation = new CredentialRepresentation();
            representation.setType(CredentialRepresentation.PASSWORD);
            representation.setTemporary(false);
            representation.setValue(newPassword);
            UserResource resource = realmResource.users().get(userId.toString());
            if (resource.toRepresentation().getEmail().equals(email)) {
                resource.resetPassword(representation);
                log.info("Password is updated for user '{}'", email);
            }
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new ForbiddenCustomException("Incorrect credentials, access to the update is denied", e);
        }
    }

    @Override
    public void resetPassword(String email, String newPassword) {
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setValue(newPassword);
        String userId = realmResource.users().search(email).getFirst().getId();
        UserResource resource = realmResource.users().get(userId);
        resource.resetPassword(credentialRepresentation);
        log.info("Password is reset for user '{}'", email);
    }

    @Override
    public void logout(UUID userId) {
        realmResource
                .users()
                .get(userId.toString())
                .logout();
        log.info("User '{}' is logged out", userId);
    }

    @Override
    public boolean deleteUser(UUID userId) {
        Response response = realmResource
                .users()
                .delete(userId.toString());
        try (response) {
            int status = response.getStatus();
            if (status >= 200 && status < 299) {
                log.info("User '{}' is deleted", userId);
                return true;
            } else {
                log.warn("Delete user status = " + status);
                return false;
            }
        }
    }


    private UUID extractId(Response response) {
        URI location = response.getLocation();
        String id = location.getPath().replaceAll(".*/", "");
        return UUID.fromString(id);
    }

    private AuthToken requestToken(MultiValueMap<String, String> params) {
        return restClient.post()
                .uri(TOKEN_URI)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(params)
                .retrieve()
                .body(AuthToken.class);
    }
}

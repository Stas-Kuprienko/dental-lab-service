package org.lab.dental.service.impl;

import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.lab.dental.exception.InternalServiceException;
import org.lab.dental.exception.NotFoundCustomException;
import org.lab.dental.service.CredentialService;
import org.lab.exception.BadRequestCustomException;
import org.lab.model.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
                                     @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String keycloakUrl,
                                     @Value("${project.variables.keycloak.client-id}") String clientId,
                                     @Value("${project.variables.keycloak.client-secret}") String clientSecret) {
        this.realmResource = realmResource;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        restClient = restClientBuilder.baseUrl(keycloakUrl).build();
    }


    @Override
    public UUID signUp(String login, String password, String name) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setValue(password);
        credential.setType(CredentialRepresentation.PASSWORD);

        UserRepresentation representation = new UserRepresentation();
        representation.setEmail(login);
        representation.setUsername(login);
        representation.setFirstName(name);
        representation.setCredentials(Collections.singletonList(credential));
        representation.setEnabled(true);
        representation.setClientRoles(Map.of(clientId, List.of("ROLE_USER")));
        representation.setEmailVerified(true);
        representation.setRequiredActions(Collections.emptyList());

        Response response = realmResource.users().create(representation);
        try (response) {
            if (response.getStatus() == 201) {
                return extractId(response);
            } else if (response.getStatus() == 409) {
                throw new BadRequestCustomException("User duplication: " + login);
            } else {
                String statusInfo = response.getStatusInfo().toString();
                throw InternalServiceException.keycloakAuthFail(login, statusInfo);
            }
        }
    }

    @Override
    public AuthToken clientLogin(String clientId, String clientSecret) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "client_credentials");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        return requestToken(params);
    }

    @Override
    public AuthToken userLogin(String email, String password) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "password");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("username", email);
        params.add("password", password);
        return requestToken(params);
    }

    @Override
    public AuthToken refresh(String refreshToken) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "refresh_token");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("refresh_token", refreshToken);
        return requestToken(params);
    }

    @Override
    public void setPassword(String login, String password) {
        //TODO
    }

    @Override
    public void deleteUser(String login) {
        List<UserRepresentation> users = realmResource.users().searchByEmail(login, true);
        if (users.isEmpty()) {
            throw NotFoundCustomException.byParams(UserRepresentation.class.getSimpleName(), Map.of("login", login));
        }
        String userId = users.getFirst().getId();
        realmResource.users().get(userId).remove();
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

package org.lab.dental.exception;

public class InternalServiceException extends RuntimeException {

    private static final String KEYCLOAK_CREATION_FAIL = "Couldn't register user in Keycloak: %s (Cause: %s)";

    public InternalServiceException(String message) {
        super(message);
    }

    public InternalServiceException(Throwable cause) {
        super(cause);
    }

    public InternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public static InternalServiceException keycloakAuthFail(String login, String cause) {
        String message = KEYCLOAK_CREATION_FAIL.formatted(login, cause);
        return new InternalServiceException(message);
    }
}

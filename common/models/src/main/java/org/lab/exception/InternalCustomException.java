package org.lab.exception;

public class InternalCustomException extends RuntimeException {

    private static final String KEYCLOAK_CREATION_FAIL = "Couldn't register user in Keycloak: %s (Cause: %s)";

    public InternalCustomException(String message) {
        super(message);
    }

    public InternalCustomException(Throwable cause) {
        super(cause);
    }

    public InternalCustomException(String message, Throwable cause) {
        super(message, cause);
    }

    public static InternalCustomException keycloakAuthFail(String login, String cause) {
        String message = KEYCLOAK_CREATION_FAIL.formatted(login, cause);
        return new InternalCustomException(message);
    }
}

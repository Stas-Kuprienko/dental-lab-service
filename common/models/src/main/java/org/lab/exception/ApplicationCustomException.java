package org.lab.exception;

public class ApplicationCustomException extends RuntimeException {

    private static final String KEYCLOAK_CREATION_FAIL = "Couldn't register user in Keycloak: %s (Cause: %s)";


    public ApplicationCustomException(String message) {
        super(message);
    }

    public ApplicationCustomException(Throwable cause) {
        super(cause);
    }

    public ApplicationCustomException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ApplicationCustomException keycloakAuthFail(String login, String cause) {
        String message = KEYCLOAK_CREATION_FAIL.formatted(login, cause);
        return new ApplicationCustomException(message);
    }
}

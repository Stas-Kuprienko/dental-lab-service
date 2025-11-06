package org.lab.exception;

public class KeycloakEmailDuplicationException extends RuntimeException {

    public final String email;

    public KeycloakEmailDuplicationException(String email) {
        super("Conflict user email duplication (" + email + ')');
        this.email = email;
    }
}

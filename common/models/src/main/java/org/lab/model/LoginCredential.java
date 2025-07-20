package org.lab.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class LoginCredential implements Serializable {

    @JsonProperty("token")
    private AuthToken token;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("email")
    private String email;


    public LoginCredential() {}
}

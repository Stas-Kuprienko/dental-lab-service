package org.lab.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ResetPasswordRequest {

    @JsonProperty("email")
    private String email;

    @JsonProperty("new_password")
    private String newPassword;


    public ResetPasswordRequest() {}
}

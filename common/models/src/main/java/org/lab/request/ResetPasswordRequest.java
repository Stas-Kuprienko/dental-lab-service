package org.lab.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ResetPasswordRequest {

    @Email
    @JsonProperty("email")
    private String email;

    @NotBlank
    @JsonProperty("new_password")
    private String newPassword;


    public ResetPasswordRequest() {}
}

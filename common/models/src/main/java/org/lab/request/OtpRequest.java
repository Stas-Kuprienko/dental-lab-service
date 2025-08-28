package org.lab.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class OtpRequest {

    @NotBlank
    @JsonProperty("otp")
    private String otp;

    public OtpRequest() {}
}

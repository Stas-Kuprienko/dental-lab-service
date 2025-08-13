package org.lab.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
public class ErrorResponse implements Serializable {

    @JsonProperty("status")
    private String status;

    @JsonProperty("message")
    private String message;


    public ErrorResponse() {}
}

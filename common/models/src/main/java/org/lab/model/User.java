package org.lab.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.lab.enums.UserStatus;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class User implements Serializable {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("login")
    private String login;

    @JsonProperty("name")
    private String name;

    @JsonProperty("created_at")
    private LocalDate createdAt;

    @JsonProperty("status")
    private UserStatus status;


    public User() {}
}

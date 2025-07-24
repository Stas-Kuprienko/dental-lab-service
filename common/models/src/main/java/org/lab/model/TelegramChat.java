package org.lab.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.lab.enums.UserStatus;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class TelegramChat {

    @JsonProperty("chat_id")
    private Long chatId;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("status")
    private UserStatus status;

    @JsonProperty("created_at")
    private LocalDate createdAt;


    public TelegramChat() {}
}

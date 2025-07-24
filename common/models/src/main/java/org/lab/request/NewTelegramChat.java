package org.lab.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class NewTelegramChat {

    @JsonProperty("chat_id")
    private long chatId;

    @JsonProperty("user_id")
    private UUID userId;
}

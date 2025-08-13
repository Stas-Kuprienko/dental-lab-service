package org.lab.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class NewTelegramOtpLink {

    @JsonProperty("key")
    private String key;

    @JsonProperty("chat_id")
    private Long chatId;


    public NewTelegramOtpLink() {}
}

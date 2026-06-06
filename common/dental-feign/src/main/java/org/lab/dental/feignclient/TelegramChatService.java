package org.lab.dental.feignclient;

import org.lab.model.TelegramChat;
import org.lab.request.NewTelegramOtpLink;
import org.lab.request.OtpRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@FeignClient(url = "${project.variables.dental-lab-api.url}", path = "/telegram_chat")
public interface TelegramChatService {


    @PostMapping
    void createLink(@RequestBody NewTelegramOtpLink newTelegramOtpLink);

    @PutMapping("/link/{key}")
    void setUserIdToLink(@PathVariable("key") String key);

    @GetMapping("/link/{key}")
    String getOtpByKey(@PathVariable("key") String key);

    @PostMapping("/link/{key}")
    UUID bindTelegram(@PathVariable("key") String key, @RequestParam(value = "lang") String lang, @RequestBody OtpRequest otp);

    @GetMapping("/{chat_id}")
    TelegramChat findByChatId(@PathVariable("chat_id") long chatId);

    @GetMapping("/user")
    TelegramChat findByUserId();
}

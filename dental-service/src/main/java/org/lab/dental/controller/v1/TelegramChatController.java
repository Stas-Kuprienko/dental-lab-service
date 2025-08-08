package org.lab.dental.controller.v1;

import lombok.extern.slf4j.Slf4j;
import org.lab.dental.entity.TelegramChatEntity;
import org.lab.dental.entity.TelegramOtpLinkEntity;
import org.lab.dental.mapping.UserConverter;
import org.lab.dental.service.TelegramOtpLinkService;
import org.lab.dental.service.UserService;
import org.lab.model.TelegramChat;
import org.lab.request.NewTelegramOtpLink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/telegram_chat")
public class TelegramChatController {

    private final TelegramOtpLinkService otpLinkService;
    private final UserService userService;
    private final UserConverter userConverter;

    @Autowired
    public TelegramChatController(TelegramOtpLinkService otpLinkService, UserService userService, UserConverter userConverter) {
        this.otpLinkService = otpLinkService;
        this.userService = userService;
        this.userConverter = userConverter;
    }


    @PostMapping
    public ResponseEntity<Void> createLink(@RequestHeader("X-SERVICE-ID") String serviceId,
                                           @RequestBody NewTelegramOtpLink newTelegramOtpLink) {

        log.info("From service '{}' received request to create TelegramOtpLink", serviceId);
        otpLinkService.create(newTelegramOtpLink.getKey(), newTelegramOtpLink.getChatId());
        return ResponseEntity.ok().build();
    }


    @PutMapping("/link/{key}")
    public ResponseEntity<Void> setUserIdToLink(@RequestHeader("X-USER-ID") UUID userId,
                                                @PathVariable("key") String key) {

        otpLinkService.setUserId(key, userId);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/link/{key}")
    public ResponseEntity<String> getOtpByKey(@RequestHeader("X-USER-ID") UUID userId,
                                              @PathVariable("key") String key) {

        log.info("From user '{}' received request to get OTP", userId);
        TelegramOtpLinkEntity otpLink = otpLinkService.find(key);
        return ResponseEntity.ok(otpLink.getOtp());
    }


    @PostMapping("/link/{key}")
    public ResponseEntity<?> bindTelegram(@RequestHeader("X-USER-ID") UUID userId,
                                          @RequestBody String otp,
                                          @PathVariable("key") String key) {

        log.info("From user '{}' received request to bind TelegramChat", userId);
        TelegramOtpLinkEntity link = otpLinkService.find(key);
        if (otpLinkService.validate(link, otp)) {
            userService.addTelegram(userId, link.getChatId());
            otpLinkService.delete(key);
            return ResponseEntity.ok(userId);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("OTP is wrong!");
        }
    }


    @GetMapping("/{chat_id}")
    public ResponseEntity<TelegramChat> findByChatId(@RequestHeader("X-SERVICE-ID") String serviceId,
                                                     @PathVariable("chat_id") Long chatId) {

        log.info("From service '{}' received request to get TelegramChat: {}", serviceId, chatId);
        TelegramChatEntity entity = userService.getTelegramChat(chatId);
        return ResponseEntity.ok(userConverter.telegramChatToDto(entity));
    }
}

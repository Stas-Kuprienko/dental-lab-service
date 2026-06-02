package org.lab.dental.controller.v1;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.lab.dental.entity.TelegramOtpLinkEntity;
import org.lab.dental.service.TelegramOtpLinkService;
import org.lab.dental.service.UserService;
import org.lab.exception.BadRequestCustomException;
import org.lab.model.TelegramChat;
import org.lab.request.NewTelegramOtpLink;
import org.lab.request.OtpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@Slf4j
@Tag(name = "Telegram Chats")
@RestController
@RequestMapping("/api/v1/telegram_chat")
public class TelegramChatController {

    private final TelegramOtpLinkService otpLinkService;
    private final UserService userService;

    @Autowired
    public TelegramChatController(TelegramOtpLinkService otpLinkService, UserService userService) {
        this.otpLinkService = otpLinkService;
        this.userService = userService;
    }


    @PostMapping
    public ResponseEntity<Void> createLink(@RequestAttribute("X-SERVICE-ID") String serviceId,
                                           @RequestBody NewTelegramOtpLink newTelegramOtpLink) {
        log.info("From service '{}' received request to create TelegramOtpLink", serviceId);
        otpLinkService.create(newTelegramOtpLink.getKey(), newTelegramOtpLink.getChatId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/link/{key}")
    public ResponseEntity<Void> setUserIdToLink(@RequestAttribute("X-USER-ID") UUID userId,
                                                @PathVariable("key") String key) {
        log.info("From user '{}' received request to set user ID to Telegram OTP", userId);
        otpLinkService.setUserId(key, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/link/{key}")
    public ResponseEntity<String> getOtpByKey(@RequestAttribute("X-USER-ID") UUID userId,
                                              @PathVariable("key") String key) {
        log.info("From user '{}' received request to get Telegram OTP", userId);
        TelegramOtpLinkEntity otpLink = otpLinkService.find(key);
        return ResponseEntity.ok(otpLink.getOtp());
    }

    @PostMapping("/link/{key}")
    public ResponseEntity<UUID> bindTelegram(@RequestAttribute("X-SERVICE-ID") String serviceId,
                                             @PathVariable("key") String key,
                                             @RequestParam(value = "lang", defaultValue = "RU") String lang,
                                             @RequestBody OtpRequest otp) {
        log.info("From service '{}' received request to bind TelegramChat", serviceId);
        TelegramOtpLinkEntity link = otpLinkService.find(key);
        if (otpLinkService.validate(link, otp.getOtp())) {
            userService.addTelegram(link.getUserId(), link.getChatId(), lang);
            otpLinkService.delete(key);
            return ResponseEntity.ok(link.getUserId());
        } else {
            throw new BadRequestCustomException("OTP is wrong!");
        }
    }

    @GetMapping("/{chat_id}")
    public ResponseEntity<TelegramChat> findByChatId(@RequestAttribute("X-SERVICE-ID") String serviceId,
                                                     @PathVariable("chat_id") Long chatId) {
        log.info("From service '{}' received request to get TelegramChat: {}", serviceId, chatId);
        TelegramChat telegramChat = userService.getTelegramChat(chatId);
        return ResponseEntity.ok(telegramChat);
    }

    @GetMapping("/user")
    public ResponseEntity<TelegramChat> findByUserId(@RequestAttribute("X-USER-ID") UUID userId) {
        log.info("From user '{}' received request to get TelegramChat", userId);
        TelegramChat telegramChat = userService.getTelegramChat(userId);
        return ResponseEntity.ok(telegramChat);
    }
}

package org.lab.dental.controller.v1;

import org.lab.dental.service.TelegramOtpLinkService;
import org.lab.dental.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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



}

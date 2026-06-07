package org.lab.uimvc.controller;

import org.lab.dental.feignclient.TelegramChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/telegram-bind")
public class TelegramBindController {

    private final TelegramChatService telegramChatService;

    @Autowired
    public TelegramBindController(TelegramChatService telegramChatService) {
        this.telegramChatService = telegramChatService;
    }


    @GetMapping("/link/{key}")
    public String telegramBindPage(@PathVariable("key") String key, Model model) {
        telegramChatService.setUserIdToLink(key);
        String otp = telegramChatService.getOtpByKey(key);
        model.addAttribute("otp", otp);
        return "telegram-otp";
    }
}

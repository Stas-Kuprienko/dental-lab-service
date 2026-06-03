package org.lab.telegram_bot.controller;

import lombok.extern.slf4j.Slf4j;
import org.lab.event.EventMessage;
import org.lab.telegram_bot.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }


    @PostMapping
    public ResponseEntity<String> sendMessage(@RequestBody EventMessage message) {
        log.info("Received message: ID='{}', chatID={}", message.getId(), message.getChatId());
        notificationService.sendMessageToChat(message);
        return ResponseEntity.ok(message.getId().toString());
    }
}

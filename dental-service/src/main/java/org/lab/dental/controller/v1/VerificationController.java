package org.lab.dental.controller.v1;

import lombok.extern.slf4j.Slf4j;
import org.lab.dental.entity.EmailVerificationTokenEntity;
import org.lab.dental.entity.TelegramChatEntity;
import org.lab.dental.service.UserService;
import org.lab.dental.service.VerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/verification")
public class VerificationController {

    private final VerificationService verificationService;
    private final UserService userService;

    @Autowired
    public VerificationController(VerificationService verificationService, UserService userService) {
        this.verificationService = verificationService;
        this.userService = userService;
    }


    @PostMapping("/email")
    public ResponseEntity<Void> sendVerificationLink(@RequestHeader("X-USER-ID") UUID userId,
                                                     @RequestBody String email,
                                                     @RequestParam(value = "to-change", defaultValue = "false") boolean toChange) {
        verificationService.createForUserId(userId, email, toChange);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/telegram-otp")
    public ResponseEntity<Void> sendTelegramOtp(@RequestHeader("X-USER-ID") UUID userId,
                                                @RequestBody String email) {
        TelegramChatEntity telegramChat = userService.getTelegramChat(userId);
        verificationService.createTelegramOtpForUserId(userId, email, telegramChat.getChatId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/email/{token}")
    public ResponseEntity<Boolean> verifyUserEmail(@RequestHeader("X-USER-ID") UUID userId,
                                                   @PathVariable("token") String token,
                                                   @RequestParam(value = "to-change", defaultValue = "false") boolean toChange) {
        boolean result;
        if (toChange) {
            result = verificationService.verifyForChangeEmail(userId, token);
        } else {
            result = verificationService.verifyUserEmail(userId, token);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/email/{email}")
    public ResponseEntity<Boolean> isVerified(@RequestHeader("X-USER-ID") UUID userId,
                                              @PathVariable("email") String email) {
        EmailVerificationTokenEntity verificationToken = verificationService.getByUserId(userId);
        if (verificationToken.getEmail().equals(email)) {
            if (verificationToken.isVerified()) {
                verificationService.deleteByUserId(userId);
                return ResponseEntity.ok(true);
            }
        }
        return ResponseEntity.ok(false);
    }
}

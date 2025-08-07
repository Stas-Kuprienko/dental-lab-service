package org.lab.dental.service;

import java.util.UUID;

public interface TelegramOtpLinkService {
    void create(String key, UUID userId, Long chatId);

    boolean validate(String key, UUID userId, Long chatId, String otp);

    void delete(String key);
}

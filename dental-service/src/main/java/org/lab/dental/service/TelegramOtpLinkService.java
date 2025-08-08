package org.lab.dental.service;

import org.lab.dental.entity.TelegramOtpLinkEntity;
import java.util.UUID;

public interface TelegramOtpLinkService {

    void create(String key, Long chatId);

    void setUserId(String key, UUID userId);

    TelegramOtpLinkEntity find(String key);

    boolean validate(TelegramOtpLinkEntity telegramOtpLink, String otp);

    void delete(String key);
}

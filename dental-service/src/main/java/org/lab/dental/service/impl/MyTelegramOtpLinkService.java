package org.lab.dental.service.impl;

import org.lab.dental.entity.TelegramOtpLinkEntity;
import org.lab.dental.repository.TelegramOtpLinkRepository;
import org.lab.dental.service.TelegramOtpLinkService;
import org.lab.dental.util.NumericCodeGenerator;
import org.lab.exception.BadRequestCustomException;
import org.lab.exception.NotFoundCustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class MyTelegramOtpLinkService implements TelegramOtpLinkService {

    private static final int linkDurationInMinutes = 10;
    private static final int otpLength = 5;

    private final TelegramOtpLinkRepository repository;
    private final NumericCodeGenerator codeGenerator;


    @Autowired
    public MyTelegramOtpLinkService(TelegramOtpLinkRepository repository, NumericCodeGenerator codeGenerator) {
        this.repository = repository;
        this.codeGenerator = codeGenerator;
    }


    @Override
    public void create(String key, Long chatId) {
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(linkDurationInMinutes);
        String otp = codeGenerator.generateNumericCode(otpLength);
        TelegramOtpLinkEntity link = TelegramOtpLinkEntity.builder()
                .key(key)
                .chatId(chatId)
                .otp(otp)
                .expiresAt(expiresAt)
                .build();
        repository.save(link);
    }

    @Override
    public void setUserId(String key, UUID userId) {
        repository.setUserId(key, userId);
    }

    @Override
    public TelegramOtpLinkEntity find(String key) {
        Optional<TelegramOtpLinkEntity> optionalTelegramLink = repository.findByKey(key);
        if (optionalTelegramLink.isEmpty()) {
            throw new NotFoundCustomException("Telegram link is not found");
        }
        TelegramOtpLinkEntity link = optionalTelegramLink.get();
        if (link.getUserId() == null) {
            throw new BadRequestCustomException("Telegram link doesn't have UserID");
        }
        return link;
    }

    @Override
    public boolean validate(TelegramOtpLinkEntity otpLink, String otp) {
        if (LocalDateTime.now().isAfter(otpLink.getExpiresAt())) {
            throw new BadRequestCustomException("Telegram link is expired");
        }
        return otp.equals(otpLink.getOtp());
    }

    @Override
    public void delete(String key) {
        repository.deleteById(key);
    }
}

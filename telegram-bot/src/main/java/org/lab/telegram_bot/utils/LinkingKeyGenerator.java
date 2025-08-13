package org.lab.telegram_bot.utils;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;

@Component
public class LinkingKeyGenerator {

    private static final String FRIENDLY_CHARS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final SecureRandom random = new SecureRandom();


    public String generate(int length) {
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(FRIENDLY_CHARS.charAt(random.nextInt(FRIENDLY_CHARS.length())));
        }
        return code.toString();
    }
}

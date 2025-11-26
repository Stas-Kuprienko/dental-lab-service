package org.lab.telegram_bot.utils;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;

@Component
public class LinkingKeyGenerator {

    private static final String CHARS = "0123456789AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz-_";
    private static final SecureRandom random = new SecureRandom();


    public String generate(int length) {
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return code.toString();
    }
}

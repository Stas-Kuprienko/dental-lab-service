package org.lab.telegram_bot.utils;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class LinkingKeyGenerator {


    public String generate() {
        return UUID.randomUUID().toString();
    }
}

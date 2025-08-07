package org.lab.dental.util;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;

@Component
public class NumericCodeGenerator {

    private static final SecureRandom random = new SecureRandom();


    public String generateNumericCode(int length) {
        StringBuilder code = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10));
        }

        return code.toString();
    }
}

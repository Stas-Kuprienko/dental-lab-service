package org.lab.dental.util;

import org.lab.exception.ApplicationCustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class AESCryptoUtility {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    private final String SECRET_KEY;


    @Autowired
    public AESCryptoUtility(@Value("${project.encryption.secret-key}") String secretKey) {
        SECRET_KEY = secretKey;
    }


    public String encrypt(String value, String initVector) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(Base64.getDecoder().decode(initVector));
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] bytes = cipher.doFinal(value.getBytes());
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new ApplicationCustomException(e);
        }
    }

    public String decrypt(String encryptedValue, String initVector) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(Base64.getDecoder().decode(initVector));
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] bytes = cipher.doFinal(Base64.getDecoder().decode(encryptedValue));
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new ApplicationCustomException(e);
        }
    }
}

package com.project.edusync.common.settings.security;

import com.project.edusync.common.exception.EdusyncException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Component
@Slf4j
public class AppSettingCryptoService {

    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;

    @Value("${APP_SETTINGS_ENCRYPTION_KEY:}")
    private String encryptionKey;

    public String encrypt(String plainText) {
        ensureKeyPresent();
        try {
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, buildKey(), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encrypted.length);
            byteBuffer.put(iv);
            byteBuffer.put(encrypted);
            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception ex) {
            log.error("Failed to encrypt app setting value", ex);
            throw new EdusyncException("Unable to encrypt setting value.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String decrypt(String cipherText) {
        ensureKeyPresent();
        try {
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            byte[] iv = new byte[IV_LENGTH];
            byte[] encrypted = new byte[decoded.length - IV_LENGTH];

            System.arraycopy(decoded, 0, iv, 0, IV_LENGTH);
            System.arraycopy(decoded, IV_LENGTH, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, buildKey(), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            log.error("Failed to decrypt app setting value", ex);
            throw new EdusyncException("Unable to decrypt setting value.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private SecretKeySpec buildKey() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] key = digest.digest(encryptionKey.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(key, "AES");
    }

    private void ensureKeyPresent() {
        if (encryptionKey == null || encryptionKey.isBlank()) {
            throw new EdusyncException("APP_SETTINGS_ENCRYPTION_KEY is not configured on server.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}


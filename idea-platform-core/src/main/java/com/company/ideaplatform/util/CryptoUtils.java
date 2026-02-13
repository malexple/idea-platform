package com.company.ideaplatform.util;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Утилита шифрования секретных настроек.
 *
 * Алгоритм: AES-256-GCM + PBKDF2-HMAC-SHA512.
 *
 * Формат зашифрованного значения в БД:
 *   ENC{base64(salt[32] + iv[12] + ciphertext + gcmTag[16])}
 *
 * Мастер-ключ берётся из переменной окружения SETTINGS_MASTER_KEY.
 * Ключ AES-256 деривируется через PBKDF2 с уникальным salt для каждого значения.
 *
 * Безопасность:
 * - AES-256-GCM: аутентифицированное шифрование (OWASP рекомендация)
 * - PBKDF2-HMAC-SHA512 310 000 итераций: защита от брутфорса мастер-ключа
 * - Уникальный 256-bit salt: каждое значение шифруется своим производным ключом
 * - Уникальный 96-bit IV: никогда не повторяется
 * - GCM Tag 128 бит: гарантия целостности данных
 */
@Component
@Slf4j
public class CryptoUtils {

    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final String KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA512";
    private static final String AES = "AES";

    // --- Параметры безопасности (максимальные) ---
    private static final int PBKDF2_ITERATIONS = 310_000;   // OWASP 2025 рекомендация
    private static final int AES_KEY_LENGTH = 256;           // AES-256
    private static final int SALT_LENGTH = 32;               // 256 бит
    private static final int IV_LENGTH = 12;                 // 96 бит (рекомендация NIST для GCM)
    private static final int GCM_TAG_LENGTH = 128;           // 128 бит

    private static final String ENCRYPTED_PREFIX = "ENC{";
    private static final String ENCRYPTED_SUFFIX = "}";

    @Value("${app.settings.master-key:}")
    private String masterKey;

    private final SecureRandom secureRandom = new SecureRandom();
    private boolean enabled;

    @PostConstruct
    void init() {
        if (StringUtils.isBlank(masterKey)) {
            log.warn("========================================");
            log.warn("  WARNING: SETTINGS_MASTER_KEY not set!");
            log.warn("  Sensitive settings stored in PLAIN TEXT.");
            log.warn("  Set SETTINGS_MASTER_KEY env variable for production.");
            log.warn("========================================");
            enabled = false;
        } else if (masterKey.length() < 32) {
            log.error("SETTINGS_MASTER_KEY is too short ({}). Minimum 32 characters required.", masterKey.length());
            enabled = false;
        } else {
            enabled = true;
            log.info("CryptoUtils initialized: AES-256-GCM + PBKDF2-HMAC-SHA512 ({} iterations)", PBKDF2_ITERATIONS);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Проверяет, является ли значение зашифрованным.
     */
    public boolean isEncrypted(String value) {
        return value != null && value.startsWith(ENCRYPTED_PREFIX) && value.endsWith(ENCRYPTED_SUFFIX);
    }

    /**
     * Шифрует строку.
     * Каждый вызов генерирует уникальные salt и IV → одно и то же значение
     * будет зашифровано по-разному каждый раз.
     */
    public String encrypt(String plainText) {
        if (plainText == null || !enabled) {
            return plainText;
        }
        try {
            // 1. Генерируем уникальный salt
            byte[] salt = new byte[SALT_LENGTH];
            secureRandom.nextBytes(salt);

            // 2. Деривируем ключ AES-256 из мастер-ключа через PBKDF2
            SecretKey aesKey = deriveKey(masterKey.toCharArray(), salt);

            // 3. Генерируем уникальный IV
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            // 4. Шифруем AES-256-GCM
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // 5. Собираем: salt + iv + ciphertext (включает GCM tag)
            ByteBuffer buffer = ByteBuffer.allocate(SALT_LENGTH + IV_LENGTH + cipherText.length);
            buffer.put(salt);
            buffer.put(iv);
            buffer.put(cipherText);

            // 6. Кодируем в base64 и оборачиваем в ENC{...}
            String encoded = Base64.getEncoder().encodeToString(buffer.array());
            return ENCRYPTED_PREFIX + encoded + ENCRYPTED_SUFFIX;

        } catch (Exception e) {
            log.error("Encryption failed: {}", e.getMessage());
            throw new RuntimeException("Failed to encrypt sensitive setting", e);
        }
    }

    /**
     * Расшифровывает строку.
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null || !isEncrypted(encryptedText) || !enabled) {
            return encryptedText;
        }
        try {
            // 1. Извлекаем base64 из ENC{...}
            String base64 = encryptedText.substring(
                    ENCRYPTED_PREFIX.length(),
                    encryptedText.length() - ENCRYPTED_SUFFIX.length()
            );
            byte[] combined = Base64.getDecoder().decode(base64);

            // 2. Разбираем: salt + iv + ciphertext
            ByteBuffer buffer = ByteBuffer.wrap(combined);

            byte[] salt = new byte[SALT_LENGTH];
            buffer.get(salt);

            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);

            byte[] cipherText = new byte[buffer.remaining()];
            buffer.get(cipherText);

            // 3. Деривируем тот же ключ из мастер-ключа + salt
            SecretKey aesKey = deriveKey(masterKey.toCharArray(), salt);

            // 4. Расшифровываем
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] plainText = cipher.doFinal(cipherText);

            return new String(plainText, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("Decryption failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * PBKDF2-HMAC-SHA512 деривация ключа.
     * 310 000 итераций — рекомендация OWASP 2025 для PBKDF2-HMAC-SHA256.
     * С SHA512 это ещё надёжнее.
     */
    private SecretKey deriveKey(char[] masterPassword, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM);
        KeySpec spec = new PBEKeySpec(masterPassword, salt, PBKDF2_ITERATIONS, AES_KEY_LENGTH);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, AES);
    }
}

package com.company.ideaplatform.config;

import com.company.ideaplatform.entity.SystemSetting;
import com.company.ideaplatform.repository.SystemSettingRepository;
import com.company.ideaplatform.util.CryptoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettingsEncryptionMigrator {

    private final SystemSettingRepository repository;
    private final CryptoUtils cryptoUtils;

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "keycloak.client-secret"
    );

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void migrateUnencryptedSecrets() {
        if (!cryptoUtils.isEnabled()) {
            log.info("Encryption not enabled, skipping secrets migration");
            return;
        }

        List<SystemSetting> allSettings = repository.findAll();
        int migrated = 0;

        for (SystemSetting setting : allSettings) {
            String key = setting.getKey();
            String value = setting.getValue();

            if (isSensitive(key) && value != null && !cryptoUtils.isEncrypted(value)) {
                String encrypted = cryptoUtils.encrypt(value);
                setting.setValue(encrypted);
                repository.save(setting);
                migrated++;
                log.info("Encrypted existing setting: {}", key);
            }
        }

        if (migrated > 0) {
            log.info("Migrated {} sensitive settings to encrypted storage", migrated);
        } else {
            log.info("All sensitive settings already encrypted");
        }
    }

    private boolean isSensitive(String key) {
        if (SENSITIVE_KEYS.contains(key)) return true;
        if (key == null) return false;
        String lower = key.toLowerCase();
        return lower.contains("secret") || lower.contains("password") || lower.contains("token");
    }
}

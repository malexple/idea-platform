package com.company.ideaplatform.service;

import com.company.ideaplatform.entity.SystemSetting;
import com.company.ideaplatform.plugin.PluginSettingService;
import com.company.ideaplatform.repository.SystemSettingRepository;
import com.company.ideaplatform.util.CryptoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemSettingService implements PluginSettingService {

    private final SystemSettingRepository repository;
    private final CryptoUtils cryptoUtils;

    /**
     * Ключи, значения которых ВСЕГДА шифруются в БД.
     */
    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "keycloak.client-secret"
    );

    // ===================== READ =====================

    @Override
    public String get(String key) {
        return repository.findByKey(key)
                .map(s -> decryptIfNeeded(s.getValue()))
                .orElse(null);
    }

    @Override
    public String get(String key, String defaultValue) {
        return repository.findByKey(key)
                .map(s -> decryptIfNeeded(s.getValue()))
                .orElse(defaultValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value.trim());
    }

    @Override
    public int getInt(String key, int defaultValue) {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("Cannot parse int setting '{}' = '{}', using default: {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    @Override
    public boolean isPluginEnabled(String pluginId) {
        return getBoolean(pluginId + ".enabled", false);
    }

    @Override
    public Map<String, String> getByPrefix(String prefix) {
        return repository.findByKeyStartingWith(prefix).stream()
                .collect(Collectors.toMap(
                        SystemSetting::getKey,
                        s -> s.getValue() != null ? decryptIfNeeded(s.getValue()) : ""
                ));
    }

    // ===================== WRITE =====================

    @Override
    @Transactional
    public void set(String key, String value) {
        SystemSetting setting = repository.findByKey(key).orElse(null);
        if (setting == null) {
            setting = new SystemSetting();
            setting.setKey(key);
            setting.setGroup(extractGroup(key));
        }
        setting.setValue(encryptIfNeeded(key, value));
        repository.save(setting);
    }

    /**
     * Сохранить настройку с описанием и группой.
     */
    @Transactional
    public void set(String key, String value, String description, String group) {
        SystemSetting setting = repository.findByKey(key).orElse(null);
        if (setting == null) {
            setting = new SystemSetting();
            setting.setKey(key);
        }
        setting.setValue(encryptIfNeeded(key, value));
        setting.setDescription(description);
        setting.setGroup(group);
        repository.save(setting);
    }

    /**
     * Получить все настройки по группе.
     */
    public List<SystemSetting> getByGroup(String group) {
        return repository.findByGroupOrderByKey(group);
    }

    /**
     * Инициализировать настройку, если она ещё не существует.
     */
    @Transactional
    public void initIfAbsent(String key, String defaultValue, String description, String group) {
        if (!repository.existsByKey(key)) {
            SystemSetting setting = SystemSetting.builder()
                    .key(key)
                    .value(encryptIfNeeded(key, defaultValue))
                    .description(description)
                    .group(group)
                    .build();
            repository.save(setting);
            log.info("Initialized setting: {} = {}", key, isSensitive(key) ? "***" : defaultValue);
        }
    }

    // ===================== ENCRYPTION =====================

    private boolean isSensitive(String key) {
        if (key == null) return false;
        if (SENSITIVE_KEYS.contains(key)) return true;
        String lower = key.toLowerCase();
        return lower.contains("secret") || lower.contains("password") || lower.contains("token");
    }

    private String encryptIfNeeded(String key, String value) {
        if (value == null) return null;
        if (isSensitive(key) && cryptoUtils.isEnabled()) {
            log.debug("Encrypting sensitive setting: {}", key);
            return cryptoUtils.encrypt(value);
        }
        return value;
    }

    private String decryptIfNeeded(String storedValue) {
        if (storedValue != null && cryptoUtils.isEncrypted(storedValue)) {
            String decrypted = cryptoUtils.decrypt(storedValue);
            if (decrypted == null) {
                log.warn("Failed to decrypt setting value");
            }
            return decrypted;
        }
        return storedValue;
    }

    // ===================== UTILS =====================

    private String extractGroup(String key) {
        int dot = key.indexOf('.');
        return dot > 0 ? key.substring(0, dot) : "general";
    }
}

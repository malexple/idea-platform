package com.company.ideaplatform.service;

import com.company.ideaplatform.entity.SystemSetting;
import com.company.ideaplatform.plugin.PluginSettingService;
import com.company.ideaplatform.repository.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemSettingService implements PluginSettingService {

    private final SystemSettingRepository repository;

    @Override
    public String get(String key) {
        return repository.findByKey(key)
                .map(SystemSetting::getValue)
                .orElse(null);
    }

    @Override
    public String get(String key, String defaultValue) {
        return repository.findByKey(key)
                .map(SystemSetting::getValue)
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
    @Transactional
    public void set(String key, String value) {
        SystemSetting setting = repository.findByKey(key).orElse(null);
        if (setting == null) {
            setting = new SystemSetting();
            setting.setKey(key);
            setting.setGroup(extractGroup(key));
        }
        setting.setValue(value);
        repository.save(setting);
    }

    @Override
    public Map<String, String> getByPrefix(String prefix) {
        return repository.findByKeyStartingWith(prefix).stream()
                .collect(Collectors.toMap(SystemSetting::getKey, s -> s.getValue() != null ? s.getValue() : ""));
    }

    @Override
    public boolean isPluginEnabled(String pluginId) {
        return getBoolean(pluginId + ".enabled", false);
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
        setting.setValue(value);
        setting.setDescription(description);
        setting.setGroup(group);
        repository.save(setting);
    }

    /**
     * Получить все настройки по группе.
     */
    public java.util.List<SystemSetting> getByGroup(String group) {
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
                    .value(defaultValue)
                    .description(description)
                    .group(group)
                    .build();
            repository.save(setting);
            log.info("Initialized setting: {} = {}", key, defaultValue);
        }
    }

    private String extractGroup(String key) {
        int dot = key.indexOf('.');
        return dot > 0 ? key.substring(0, dot) : "general";
    }
}

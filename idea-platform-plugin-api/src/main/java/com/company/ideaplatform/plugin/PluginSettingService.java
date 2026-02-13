package com.company.ideaplatform.plugin;

import java.util.Map;

/**
 * Интерфейс для чтения/записи настроек плагинов.
 * Реализуется в core-модуле, инжектится в плагины.
 */
public interface PluginSettingService {

    /**
     * Получить значение настройки по ключу.
     */
    String get(String key);

    /**
     * Получить значение настройки или значение по умолчанию.
     */
    String get(String key, String defaultValue);

    /**
     * Получить boolean-настройку.
     */
    boolean getBoolean(String key, boolean defaultValue);

    /**
     * Получить int-настройку.
     */
    int getInt(String key, int defaultValue);

    /**
     * Сохранить значение настройки.
     */
    void set(String key, String value);

    /**
     * Получить все настройки с заданным префиксом.
     * Например: getByPrefix("keycloak.") вернёт все настройки Keycloak.
     */
    Map<String, String> getByPrefix(String prefix);

    /**
     * Проверить, включён ли плагин по его ID.
     */
    boolean isPluginEnabled(String pluginId);
}

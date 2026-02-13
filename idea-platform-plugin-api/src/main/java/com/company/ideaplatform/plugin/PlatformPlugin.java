package com.company.ideaplatform.plugin;

import java.util.List;

/**
 * Базовый интерфейс для всех плагинов платформы.
 * Каждый плагин (Keycloak, Jira и т.д.) реализует этот интерфейс.
 */
public interface PlatformPlugin {

    /**
     * Уникальный идентификатор плагина, например "keycloak", "jira".
     */
    String getId();

    /**
     * Отображаемое имя плагина для UI, например "Keycloak SSO".
     */
    String getDisplayName();

    /**
     * Описание плагина для UI.
     */
    String getDescription();

    /**
     * Группа настроек: "auth", "integration", "notification" и т.д.
     */
    String getGroup();

    /**
     * Список описателей настроек, которые плагин требует для работы.
     */
    List<PluginSettingDescriptor> getSettingDescriptors();

    /**
     * Вызывается при включении плагина.
     */
    default void onEnable() {}

    /**
     * Вызывается при выключении плагина.
     */
    default void onDisable() {}
}

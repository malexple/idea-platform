package com.company.ideaplatform.plugin;

/**
 * Описатель одной настройки плагина.
 * Используется для автоматического построения формы в админке.
 */
public class PluginSettingDescriptor {

    private final String key;
    private final String displayName;
    private final String description;
    private final SettingType type;
    private final String defaultValue;
    private final boolean required;

    public PluginSettingDescriptor(String key, String displayName, String description,
                                   SettingType type, String defaultValue, boolean required) {
        this.key = key;
        this.displayName = displayName;
        this.description = description;
        this.type = type;
        this.defaultValue = defaultValue;
        this.required = required;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public SettingType getType() {
        return type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean isRequired() {
        return required;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String key;
        private String displayName;
        private String description = "";
        private SettingType type = SettingType.STRING;
        private String defaultValue = "";
        private boolean required = false;

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder type(SettingType type) {
            this.type = type;
            return this;
        }

        public Builder defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder required(boolean required) {
            this.required = required;
            return this;
        }

        public PluginSettingDescriptor build() {
            return new PluginSettingDescriptor(key, displayName, description, type, defaultValue, required);
        }
    }
}

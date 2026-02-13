package com.company.ideaplatform.config;

import com.company.ideaplatform.plugin.PlatformPlugin;
import com.company.ideaplatform.plugin.PluginSettingDescriptor;
import com.company.ideaplatform.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PluginRegistrar {

    private final SystemSettingService settingService;
    private final List<PlatformPlugin> plugins;

    @EventListener(ApplicationReadyEvent.class)
    public void registerPlugins() {
        if (plugins.isEmpty()) {
            log.info("No plugins found");
            return;
        }

        for (PlatformPlugin plugin : plugins) {
            log.info("Registering plugin: {} ({})", plugin.getDisplayName(), plugin.getId());

            // Инициализируем настройку enabled
            settingService.initIfAbsent(
                    plugin.getId() + ".enabled",
                    "false",
                    "Включить плагин " + plugin.getDisplayName(),
                    plugin.getId()
            );

            // Инициализируем все настройки плагина
            for (PluginSettingDescriptor descriptor : plugin.getSettingDescriptors()) {
                settingService.initIfAbsent(
                        descriptor.getKey(),
                        descriptor.getDefaultValue(),
                        descriptor.getDescription(),
                        plugin.getId()
                );
            }
        }
    }
}

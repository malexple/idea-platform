package com.company.ideaplatform.keycloak;

import com.company.ideaplatform.plugin.AuthPlugin;
import com.company.ideaplatform.plugin.PluginSettingDescriptor;
import com.company.ideaplatform.plugin.SettingType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KeycloakAuthPlugin implements AuthPlugin {

    @Override
    public String getId() {
        return "keycloak";
    }

    @Override
    public String getDisplayName() {
        return "Keycloak SSO";
    }

    @Override
    public String getDescription() {
        return "Аутентификация через Keycloak (OpenID Connect). Заменяет локальную форму входа.";
    }

    @Override
    public String getGroup() {
        return "auth";
    }

    @Override
    public List<PluginSettingDescriptor> getSettingDescriptors() {
        return List.of(
                PluginSettingDescriptor.builder()
                        .key("keycloak.server-url")
                        .displayName("URL сервера Keycloak")
                        .description("Например: http://localhost:9090")
                        .type(SettingType.URL)
                        .defaultValue("http://localhost:9090")
                        .required(true)
                        .build(),
                PluginSettingDescriptor.builder()
                        .key("keycloak.realm")
                        .displayName("Realm")
                        .description("Имя Realm в Keycloak")
                        .type(SettingType.STRING)
                        .defaultValue("idea-platform")
                        .required(true)
                        .build(),
                PluginSettingDescriptor.builder()
                        .key("keycloak.client-id")
                        .displayName("Client ID")
                        .description("ID клиентского приложения в Keycloak")
                        .type(SettingType.STRING)
                        .defaultValue("idea-platform-app")
                        .required(true)
                        .build(),
                PluginSettingDescriptor.builder()
                        .key("keycloak.client-secret")
                        .displayName("Client Secret")
                        .description("Секрет клиентского приложения")
                        .type(SettingType.PASSWORD)
                        .defaultValue("")
                        .required(true)
                        .build(),
                PluginSettingDescriptor.builder()
                        .key("keycloak.admin-email")
                        .displayName("Email администратора")
                        .description("Email пользователя, который автоматически получит роль ADMIN при первом входе через Keycloak. Для чистого стенда (без seed-данных).")
                        .type(SettingType.STRING)
                        .defaultValue("")
                        .required(false)
                        .build()
        );
    }


    @Override
    public void configureHttpSecurity(HttpSecurity http) throws Exception {
        // OAuth2 Login конфигурация будет подхвачена через KeycloakSecurityConfig
        http.oauth2Login(oauth2 -> oauth2
                .defaultSuccessUrl("/ideas", true)
        );
    }

    @Override
    public String getLoginRedirectUrl() {
        return "/oauth2/authorization/keycloak";
    }


}

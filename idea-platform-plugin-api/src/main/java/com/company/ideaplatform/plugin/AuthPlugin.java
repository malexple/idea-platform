package com.company.ideaplatform.plugin;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * Специализированный интерфейс для плагинов аутентификации.
 * Позволяет плагину встроить свою конфигурацию в SecurityFilterChain.
 */
public interface AuthPlugin extends PlatformPlugin {

    /**
     * Дополняет HttpSecurity конфигурацией OAuth2/OIDC.
     * Вызывается из SecurityConfig, если плагин включён.
     */
    void configureHttpSecurity(HttpSecurity http) throws Exception;

    /**
     * URL, на который перенаправлять пользователя для логина через этот плагин.
     * Например: "/oauth2/authorization/keycloak"
     */
    String getLoginRedirectUrl();
}

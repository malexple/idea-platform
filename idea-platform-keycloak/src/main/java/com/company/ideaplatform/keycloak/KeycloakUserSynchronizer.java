package com.company.ideaplatform.keycloak;

/**
 * Интерфейс для синхронизации Keycloak-пользователя с локальной БД.
 * Реализуется в core-модуле, инжектится сюда.
 */
public interface KeycloakUserSynchronizer {

    /**
     * Создаёт или обновляет пользователя в БД по данным из Keycloak.
     *
     * @param email      email из токена
     * @param displayName имя из токена
     * @param externalId  subject (sub) из токена
     * @param adminEmail  email из настройки keycloak.admin-email (может быть null)
     * @return имя роли пользователя (например "ADMIN", "USER")
     */
    String synchronizeUser(String email, String displayName, String externalId, String adminEmail);
}

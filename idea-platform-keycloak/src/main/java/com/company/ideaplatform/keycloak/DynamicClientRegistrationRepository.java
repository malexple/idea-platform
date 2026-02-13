package com.company.ideaplatform.keycloak;

import com.company.ideaplatform.plugin.PluginSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DynamicClientRegistrationRepository implements ClientRegistrationRepository {

    private final PluginSettingService settingService;

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        if (!"keycloak".equals(registrationId)) {
            return null;
        }

        if (!settingService.isPluginEnabled("keycloak")) {
            log.warn("Keycloak plugin is not enabled, returning null ClientRegistration");
            return null;
        }

        String serverUrl = settingService.get("keycloak.server-url", "http://localhost:9090");
        String realm = settingService.get("keycloak.realm", "idea-platform");
        String clientId = settingService.get("keycloak.client-id", "idea-platform-app");
        String clientSecret = settingService.get("keycloak.client-secret", "");

        String issuerUri = serverUrl + "/realms/" + realm;

        log.debug("Building Keycloak ClientRegistration: serverUrl={}, realm={}, clientId={}",
                serverUrl, realm, clientId);

        return ClientRegistration.withRegistrationId("keycloak")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .scope("openid", "profile", "email")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri(issuerUri + "/protocol/openid-connect/auth")
                .tokenUri(issuerUri + "/protocol/openid-connect/token")
                .userInfoUri(issuerUri + "/protocol/openid-connect/userinfo")
                .jwkSetUri(issuerUri + "/protocol/openid-connect/certs")
                .issuerUri(issuerUri)
                .userNameAttributeName(IdTokenClaimNames.SUB)
                .clientName("Keycloak")
                .build();
    }
}

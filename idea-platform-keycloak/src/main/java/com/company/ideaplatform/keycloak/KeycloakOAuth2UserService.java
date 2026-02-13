package com.company.ideaplatform.keycloak;

import com.company.ideaplatform.plugin.PluginSettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class KeycloakOAuth2UserService extends OidcUserService {

    private final KeycloakUserSynchronizer userSynchronizer;
    private final PluginSettingService settingService;

    public KeycloakOAuth2UserService(KeycloakUserSynchronizer userSynchronizer,
                                     PluginSettingService settingService) {
        this.userSynchronizer = userSynchronizer;
        this.settingService = settingService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
        String sub = oidcUser.getSubject();

        // Читаем admin-email из настроек
        String adminEmail = settingService.get("keycloak.admin-email");

        log.info("Keycloak user logged in: email={}, name={}, sub={}, adminEmail={}",
                email, name, sub, adminEmail);

        String role = userSynchronizer.synchronizeUser(email, name, sub, adminEmail);

        Set<GrantedAuthority> authorities = new HashSet<>(oidcUser.getAuthorities());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
    }
}

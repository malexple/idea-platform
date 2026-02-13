package com.company.ideaplatform.keycloak;

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

    public KeycloakOAuth2UserService(KeycloakUserSynchronizer userSynchronizer) {
        this.userSynchronizer = userSynchronizer;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
        String sub = oidcUser.getSubject();

        log.info("Keycloak user logged in: email={}, name={}, sub={}", email, name, sub);

        // Синхронизируем пользователя с нашей БД и получаем роль
        String role = userSynchronizer.synchronizeUser(email, name, sub);

        // Строим authorities из роли в нашей БД
        Set<GrantedAuthority> authorities = new HashSet<>(oidcUser.getAuthorities());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
    }
}

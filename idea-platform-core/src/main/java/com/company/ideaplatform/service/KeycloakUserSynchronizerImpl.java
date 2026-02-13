package com.company.ideaplatform.service;

import com.company.ideaplatform.entity.User;
import com.company.ideaplatform.keycloak.KeycloakUserSynchronizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakUserSynchronizerImpl implements KeycloakUserSynchronizer {

    private final UserService userService;

    @Override
    public String synchronizeUser(String email, String displayName, String externalId, String adminEmail) {
        User user = userService.findOrCreateFromOAuth(email, displayName, externalId, adminEmail);
        log.info("Synchronized Keycloak user: email={}, role={}", user.getEmail(), user.getRole());
        return user.getRole().name();
    }
}

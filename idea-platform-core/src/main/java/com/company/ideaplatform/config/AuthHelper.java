package com.company.ideaplatform.config;

import com.company.ideaplatform.entity.User;
import com.company.ideaplatform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthHelper {

    private final UserService userService;

    /**
     * Получить email текущего пользователя из любого типа аутентификации.
     */
    public String getCurrentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof OidcUser oidcUser) {
            return oidcUser.getEmail();
        }
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        if (principal instanceof String s && !"anonymousUser".equals(s)) {
            return s;
        }

        return null;
    }

    /**
     * Получить текущего User из БД. null если не аутентифицирован.
     */
    public User getCurrentUser() {
        String email = getCurrentEmail();
        if (email == null) {
            return null;
        }
        try {
            return userService.findByEmail(email);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Получить ID текущего пользователя. null если не аутентифицирован.
     */
    public Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }
}

package com.company.ideaplatform.service;

import com.company.ideaplatform.entity.User;
import com.company.ideaplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("=== Login attempt for: {} ===", email);

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            log.error("User NOT FOUND: {}", email);
            throw new UsernameNotFoundException("Пользователь не найден: " + email);
        }

        log.info("User found: {}", user.getEmail());
        log.info("User active: {}", user.getActive());
        log.info("User role: {}", user.getRole());
        log.info("User auth provider: {}", user.getAuthProvider());

        if (!user.getActive()) {
            log.error("User is INACTIVE: {}", email);
            throw new UsernameNotFoundException("Пользователь деактивирован: " + email);
        }

        // Keycloak-пользователи не могут войти через form login
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            log.error("User has no local password (external auth): {}", email);
            throw new UsernameNotFoundException(
                    "Пользователь использует внешнюю аутентификацию: " + email);
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}

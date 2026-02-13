package com.company.ideaplatform.config;

import com.company.ideaplatform.plugin.AuthPlugin;
import com.company.ideaplatform.service.CustomUserDetailsService;
import com.company.ideaplatform.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final SystemSettingService settingService;
    private final List<AuthPlugin> authPlugins;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        boolean keycloakEnabled = settingService.isPluginEnabled("keycloak") && !authPlugins.isEmpty();

        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll();
            auth.requestMatchers("/debug/**").permitAll();
            auth.requestMatchers("/", "/ideas", "/ideas/{number}").permitAll();

            if (!keycloakEnabled) {
                auth.requestMatchers("/login", "/register").permitAll();
            }

            auth.requestMatchers("/admin/**").hasRole("ADMIN");
            auth.requestMatchers("/manager/**").hasAnyRole("MANAGER", "ADMIN");
            auth.requestMatchers("/review/**").hasAnyRole("REVIEWER", "ADMIN");
            auth.anyRequest().authenticated();
        });

        if (keycloakEnabled) {
            log.info("Keycloak authentication ENABLED — configuring OAuth2 Login");

            String loginRedirectUrl = authPlugins.stream()
                    .filter(p -> settingService.isPluginEnabled(p.getId()))
                    .map(AuthPlugin::getLoginRedirectUrl)
                    .findFirst()
                    .orElse("/oauth2/authorization/keycloak");

            http.oauth2Login(oauth2 -> oauth2
                    .loginPage(loginRedirectUrl)
                    .defaultSuccessUrl("/ideas", true)
            );

            // OIDC logout — разлогинивает и в Keycloak тоже
            OidcClientInitiatedLogoutSuccessHandler logoutHandler =
                    new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
            logoutHandler.setPostLogoutRedirectUri("{baseUrl}/");

            http.logout(logout -> logout
                    .logoutSuccessHandler(logoutHandler)
                    .permitAll()
            );
        } else {
            log.info("Local authentication — configuring Form Login");
            http.formLogin(form -> form
                    .loginPage("/login")
                    .defaultSuccessUrl("/ideas", true)
                    .failureUrl("/login?error=true")
                    .permitAll()
            );

            http.logout(logout -> logout
                    .logoutSuccessUrl("/login?logout=true")
                    .permitAll()
            );
        }

        http.exceptionHandling(ex -> ex
                .accessDeniedPage("/access-denied")
        );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

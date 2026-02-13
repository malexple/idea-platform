package com.company.ideaplatform.controller;

import com.company.ideaplatform.plugin.AuthPlugin;
import com.company.ideaplatform.service.DivisionService;
import com.company.ideaplatform.service.SystemSettingService;
import com.company.ideaplatform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final DivisionService divisionService;
    private final SystemSettingService settingService;
    private final List<AuthPlugin> authPlugins;

    @GetMapping("/login")
    public String login() {
        // Если Keycloak включён — редиректим на OAuth2
        if (settingService.isPluginEnabled("keycloak") && !authPlugins.isEmpty()) {
            AuthPlugin plugin = authPlugins.stream()
                    .filter(p -> settingService.isPluginEnabled(p.getId()))
                    .findFirst()
                    .orElse(null);
            if (plugin != null) {
                return "redirect:" + plugin.getLoginRedirectUrl();
            }
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        // Регистрация недоступна при Keycloak
        if (settingService.isPluginEnabled("keycloak")) {
            return "redirect:/";
        }
        model.addAttribute("teams", divisionService.getAllTeams());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String displayName,
            @RequestParam(required = false) Long teamId,
            RedirectAttributes redirectAttributes) {

        if (settingService.isPluginEnabled("keycloak")) {
            return "redirect:/";
        }

        try {
            userService.createUser(email, password, displayName, teamId);
            redirectAttributes.addFlashAttribute("success",
                    "Регистрация успешна! Теперь вы можете войти.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "auth/access-denied";
    }
}

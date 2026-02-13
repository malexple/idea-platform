package com.company.ideaplatform.controller;

import com.company.ideaplatform.entity.Division;
import com.company.ideaplatform.entity.Team;
import com.company.ideaplatform.entity.Tribe;
import com.company.ideaplatform.entity.User;
import com.company.ideaplatform.entity.enums.UserRole;
import com.company.ideaplatform.plugin.PlatformPlugin;
import com.company.ideaplatform.plugin.PluginSettingDescriptor;
import com.company.ideaplatform.service.DivisionService;
import com.company.ideaplatform.service.SystemSettingService;
import com.company.ideaplatform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final DivisionService divisionService;
    private final UserService userService;
    private final SystemSettingService settingService;
    private final List<PlatformPlugin> plugins;

    // Divisions
    @GetMapping("/divisions")
    public String divisions(Model model) {
        model.addAttribute("divisions", divisionService.getAllDivisions());
        model.addAttribute("newDivision", new Division());
        return "admin/divisions";
    }

    @PostMapping("/divisions")
    public String createDivision(@ModelAttribute Division division, RedirectAttributes redirectAttributes) {
        divisionService.createDivision(division);
        redirectAttributes.addFlashAttribute("success", "Дивизион создан");
        return "redirect:/admin/divisions";
    }

    @PostMapping("/divisions/{id}/toggle")
    public String toggleDivision(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        divisionService.toggleActive(id);
        redirectAttributes.addFlashAttribute("success", "Статус изменён");
        return "redirect:/admin/divisions";
    }

    // ==================== Tribes ====================

    @GetMapping("/tribes")
    public String tribes(Model model) {
        model.addAttribute("tribes", divisionService.getAllTribes());
        model.addAttribute("divisions", divisionService.getAllActive());
        model.addAttribute("newTribe", new Tribe());
        return "admin/tribes";
    }

    @PostMapping("/tribes")
    public String createTribe(@ModelAttribute Tribe tribe, @RequestParam Long divisionId,
                              RedirectAttributes redirectAttributes) {
        divisionService.createTribe(tribe, divisionId);
        redirectAttributes.addFlashAttribute("success", "Трайб создан");
        return "redirect:/admin/tribes";
    }

    // ==================== Teams ====================

    @GetMapping("/teams")
    public String teams(Model model) {
        model.addAttribute("teams", divisionService.getAllTeams());
        model.addAttribute("tribes", divisionService.getAllTribes());
        model.addAttribute("newTeam", new Team());
        return "admin/teams";
    }

    @PostMapping("/teams")
    public String createTeam(@ModelAttribute Team team, @RequestParam Long tribeId,
                             RedirectAttributes redirectAttributes) {
        divisionService.createTeam(team, tribeId);
        redirectAttributes.addFlashAttribute("success", "Команда создана");
        return "redirect:/admin/teams";
    }

    // ==================== Users ====================

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("roles", UserRole.values());
        return "admin/users";
    }

    @PostMapping("/users/{id}/role")
    public String updateUserRole(@PathVariable Long id, @RequestParam String role,
                                 RedirectAttributes redirectAttributes) {
        userService.updateRole(id, UserRole.valueOf(role));
        redirectAttributes.addFlashAttribute("success", "Роль обновлена");
        return "redirect:/admin/users";
    }

    // ==================== Settings (NEW) ====================

    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("plugins", plugins);
        model.addAttribute("settingService", settingService);
        return "admin/settings";
    }

    @PostMapping("/settings/{pluginId}")
    public String savePluginSettings(@PathVariable String pluginId,
                                     @RequestParam Map<String, String> params,
                                     RedirectAttributes redirectAttributes) {
        // Находим плагин
        PlatformPlugin plugin = plugins.stream()
                .filter(p -> p.getId().equals(pluginId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Плагин не найден: " + pluginId));

        // Сохраняем enabled
        String enabledKey = pluginId + ".enabled";
        String enabledValue = params.containsKey("enabled") ? "true" : "false";
        settingService.set(enabledKey, enabledValue, "Включить плагин " + plugin.getDisplayName(), pluginId);

        // Сохраняем остальные настройки
        for (PluginSettingDescriptor descriptor : plugin.getSettingDescriptors()) {
            String value = params.get(descriptor.getKey());
            if (value != null) {
                settingService.set(descriptor.getKey(), value, descriptor.getDescription(), pluginId);
            }
        }

        redirectAttributes.addFlashAttribute("success",
                "Настройки плагина «" + plugin.getDisplayName() + "» сохранены. " +
                        "Перезапустите приложение для применения изменений аутентификации.");
        return "redirect:/admin/settings";
    }
}

package com.company.ideaplatform.controller;

import com.company.ideaplatform.entity.Division;
import com.company.ideaplatform.entity.Team;
import com.company.ideaplatform.entity.Tribe;
import com.company.ideaplatform.entity.User;
import com.company.ideaplatform.entity.enums.UserRole;
import com.company.ideaplatform.service.DivisionService;
import com.company.ideaplatform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final DivisionService divisionService;
    private final UserService userService;

    // Divisions
    @GetMapping("/divisions")
    public String divisions(Model model) {
        model.addAttribute("divisions", divisionService.getAllDivisions());
        model.addAttribute("newDivision", new Division());
        return "admin/divisions";
    }

    @PostMapping("/divisions")
    public String createDivision(
            @ModelAttribute Division division,
            RedirectAttributes redirectAttributes) {
        divisionService.createDivision(division);
        redirectAttributes.addFlashAttribute("success", "Дивизион создан");
        return "redirect:/admin/divisions";
    }

    @PostMapping("/divisions/{id}/toggle")
    public String toggleDivision(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        divisionService.toggleActive(id);
        redirectAttributes.addFlashAttribute("success", "Статус изменён");
        return "redirect:/admin/divisions";
    }

    // Tribes
    @GetMapping("/tribes")
    public String tribes(Model model) {
        model.addAttribute("tribes", divisionService.getAllTribes());
        model.addAttribute("divisions", divisionService.getAllActive());
        model.addAttribute("newTribe", new Tribe());
        return "admin/tribes";
    }

    @PostMapping("/tribes")
    public String createTribe(
            @ModelAttribute Tribe tribe,
            @RequestParam Long divisionId,
            RedirectAttributes redirectAttributes) {
        divisionService.createTribe(tribe, divisionId);
        redirectAttributes.addFlashAttribute("success", "Трайб создан");
        return "redirect:/admin/tribes";
    }

    // Teams
    @GetMapping("/teams")
    public String teams(Model model) {
        model.addAttribute("teams", divisionService.getAllTeams());
        model.addAttribute("tribes", divisionService.getAllTribes());
        model.addAttribute("newTeam", new Team());
        return "admin/teams";
    }

    @PostMapping("/teams")
    public String createTeam(
            @ModelAttribute Team team,
            @RequestParam Long tribeId,
            RedirectAttributes redirectAttributes) {
        divisionService.createTeam(team, tribeId);
        redirectAttributes.addFlashAttribute("success", "Команда создана");
        return "redirect:/admin/teams";
    }

    // Users
    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("roles", UserRole.values());
        return "admin/users";
    }

    @PostMapping("/users/{id}/role")
    public String updateUserRole(
            @PathVariable Long id,
            @RequestParam UserRole role,
            RedirectAttributes redirectAttributes) {
        userService.updateRole(id, role);
        redirectAttributes.addFlashAttribute("success", "Роль обновлена");
        return "redirect:/admin/users";
    }
}

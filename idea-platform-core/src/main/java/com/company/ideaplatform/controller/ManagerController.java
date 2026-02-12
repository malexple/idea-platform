package com.company.ideaplatform.controller;

import com.company.ideaplatform.dto.InnovatorDto;
import com.company.ideaplatform.dto.ManagerDashboardDto;
import com.company.ideaplatform.dto.TeamActivityDto;
import com.company.ideaplatform.service.ManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/manager")
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
@RequiredArgsConstructor
public class ManagerController {

    private final ManagerService managerService;

    @GetMapping
    public String dashboard(Model model) {
        ManagerDashboardDto dashboard = managerService.getDashboard();
        model.addAttribute("dashboard", dashboard);
        return "manager/dashboard";
    }

    @GetMapping("/people")
    public String people(
            @RequestParam(required = false, defaultValue = "ALL") String category,
            Model model) {

        ManagerDashboardDto dashboard = managerService.getDashboard();
        List<InnovatorDto> people = managerService.getPeopleByCategory(
                "ALL".equals(category) ? null : category
        );

        model.addAttribute("dashboard", dashboard);
        model.addAttribute("people", people);
        model.addAttribute("selectedCategory", category);
        return "manager/people";
    }

    @GetMapping("/teams")
    public String teams(Model model) {
        List<TeamActivityDto> teams = managerService.getTopTeams(100);
        ManagerDashboardDto dashboard = managerService.getDashboard();

        model.addAttribute("teams", teams);
        model.addAttribute("dashboard", dashboard);
        return "manager/teams";
    }

    @GetMapping("/funnel")
    public String funnel(Model model) {
        ManagerDashboardDto dashboard = managerService.getDashboard();
        model.addAttribute("dashboard", dashboard);
        return "manager/funnel";
    }
}

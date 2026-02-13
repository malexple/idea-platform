package com.company.ideaplatform.controller;

import com.company.ideaplatform.config.AuthHelper;
import com.company.ideaplatform.dto.DashboardDto;
import com.company.ideaplatform.dto.IdeaViewDto;
import com.company.ideaplatform.dto.LeaderboardEntryDto;
import com.company.ideaplatform.service.DashboardService;
import com.company.ideaplatform.service.IdeaService;
import com.company.ideaplatform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final IdeaService ideaService;
    private final UserService userService;
    private final AuthHelper authHelper;

    @GetMapping
    public String dashboard(Model model) {
        Long currentUserId = authHelper.getCurrentUserId();
        DashboardDto dashboard = dashboardService.getDashboard();
        Page<IdeaViewDto> topIdeas = ideaService.getTopVotedIdeas(PageRequest.of(0, 10), currentUserId);
        List<LeaderboardEntryDto> leaderboard = userService.getLeaderboard(10);

        model.addAttribute("dashboard", dashboard);
        model.addAttribute("topIdeas", topIdeas);
        model.addAttribute("leaderboard", leaderboard);
        return "dashboard/index";
    }
}

package com.company.ideaplatform.controller;

import com.company.ideaplatform.config.AuthHelper;
import com.company.ideaplatform.dto.UserProfileDto;
import com.company.ideaplatform.entity.User;
import com.company.ideaplatform.entity.enums.HeroType;
import com.company.ideaplatform.service.AchievementService;
import com.company.ideaplatform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final AchievementService achievementService;
    private final AuthHelper authHelper;

    @GetMapping
    public String myProfile(Model model) {
        User user = authHelper.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        return showProfile(user.getId(), model);
    }

    @GetMapping("/{userId}")
    public String userProfile(@PathVariable Long userId, Model model) {
        return showProfile(userId, model);
    }

    private String showProfile(Long userId, Model model) {
        UserProfileDto profile = userService.getProfile(userId);
        model.addAttribute("profile", profile);
        model.addAttribute("heroTypes", HeroType.values());
        model.addAttribute("allAchievements", achievementService.getAllAchievements());
        return "profile/view";
    }
}
